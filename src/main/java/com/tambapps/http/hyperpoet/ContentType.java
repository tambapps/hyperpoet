package com.tambapps.http.hyperpoet;

import lombok.Value;
import okhttp3.MediaType;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class representing a Mime Type, used to know how to (de)serialize request/response data.
 * Wildcard type/subtypes and mime type parameters are supported
 */
@Value
public class ContentType implements Comparable<ContentType> {

  public static final String WILDCARD_TYPE = "*";
  public static final String HEADER = "Content-Type";

  public static final ContentType WILDCARD = new ContentType("*", "*");
  public static final ContentType JSON = new ContentType("application", "json");
  public static final ContentType XML = new ContentType("application", "xml");
  public static final ContentType TEXT = new ContentType("text", "plain");
  public static final ContentType HTML = new ContentType("text", "html");
  public static final ContentType BINARY = new ContentType("application", "octet-stream");
  public static final ContentType URL_ENCODED = new ContentType("application", "x-www-form-urlencoded");
  public static final ContentType MULTIPART_FORM = new ContentType("multipart", "form-data");

  public static final String PARAM_CHARSET = "charset";

  String type;
  String subtype;
  Map<String, String> parameters;

  public ContentType(String type, String subtype) {
    this(type, subtype, Collections.emptyMap());
  }

  public ContentType(String type, String subtype, Map<String, String> parameters) {
    this.type = type;
    this.subtype = subtype;
    this.parameters = Collections.unmodifiableMap(parameters);
  }

  public static ContentType valueOf(String mimeType) {
    int index = mimeType.indexOf(';');
    String fullType = (index >= 0 ? mimeType.substring(0, index) : mimeType).trim();
    if (WILDCARD_TYPE.equals(fullType)) {
      // it can happen
      fullType = "*/*";
    }
    int subIndex = fullType.indexOf('/');
    if (subIndex == -1) {
      throw new IllegalArgumentException(String.format("Invalid Mime type: '%s' does not contain '/'", mimeType));
    }
    String type = fullType.substring(0, subIndex);
    String subtype = fullType.substring(subIndex + 1);
    if (type.isEmpty()) {
      throw new IllegalArgumentException("content type doesn't have a type");
    }
    if (subtype.isEmpty()) {
      throw new IllegalArgumentException("content type doesn't have a subtype");
    }
    if (index < 0) {
      return new ContentType(type, subtype);
    }
    Map<String, String> parameters = new HashMap<>();
    Matcher matcher = Pattern.compile(";\\s*(\\w+)=\"?([^;]+)\"?").matcher(mimeType.substring(index));
    while (matcher.find()) {
      // parameter names are case-insensitive and usually lowercase
      String parameterName = matcher.group(1).toLowerCase(Locale.ENGLISH);
      String parameterValue = matcher.group(2);
      parameters.put(parameterName, parameterValue);
    }
    return new ContentType(type, subtype, parameters);
  }

  public Optional<Charset> getCharset() {
    return Optional.ofNullable(
        parameters.containsKey(PARAM_CHARSET) ? Charset.forName(parameters.get(PARAM_CHARSET)) : null
    );
  }

  public ContentType withCharset(Charset charset) {
    return withCharset(charset.name());
  }

  public ContentType withCharset(String name) {
    Map<String, String> parameters = new HashMap<>(this.parameters);
    parameters.put(PARAM_CHARSET, name);
    return new ContentType(type, subtype, parameters);
  }

  public boolean isWildcardType() {
    return WILDCARD_TYPE.equals(getType());
  }

  /**
   * Return (if any) the subtype suffix as defined in RFC 6839.
   * @return the (optional) subtype suffix
   */
  public Optional<String> getSubtypeSuffix() {
    int suffixIndex = this.subtype.lastIndexOf('+');
    if (suffixIndex != -1 && this.subtype.length() > suffixIndex) {
      return Optional.of(this.subtype.substring(suffixIndex + 1));
    }
    return Optional.empty();
  }

  /**
   * Indicates whether the getSubtype() subtype is the wildcard
   * character <code>&#42;</code> or the wildcard character followed by a suffix
   * (e.g. <code>&#42;+xml</code>).
   * @return whether the subtype is a wildcard
   */
  public boolean isWildcardSubtype() {
    return WILDCARD_TYPE.equals(getSubtype()) || getSubtype().startsWith("*+");
  }

  public MediaType toMediaType() {
    return MediaType.get(toString());
  }

  /**
   * Indicate whether this MIME Type includes the given MIME Type.
   * <p>For instance, {@code text/*} includes {@code text/plain} and {@code text/html},
   * and {@code application/*+xml} includes {@code application/soap+xml},
   * and {@code application/xml} includes {@code application/*+xml}, etc.
   * This method is <b>not</b> symmetric.
   * @param other the reference MIME Type with which to compare
   * @return {@code true} if this MIME Type includes the given MIME Type;
   * {@code false} otherwise
   */
  public boolean includes(ContentType other) {
    if (other == null) {
      return false;
    }
    if (isWildcardType() && isWildcardSubtype()) {
      // */* includes anything
      return true;
    } else if (getType().equals(other.getType())) {
      if (getSubtype().equals(other.getSubtypeSuffix().orElse(other.getSubtype()))) {
        return true;
      }
      if (isWildcardSubtype()) {
        // Wildcard with suffix, e.g. application/*+xml
        int thisPlusIdx = getSubtype().lastIndexOf('+');
        if (thisPlusIdx == -1) {
          return true;
        } else {
          // application/*+xml includes application/soap+xml
          int otherPlusIdx = other.getSubtype().lastIndexOf('+');
          if (otherPlusIdx != -1) {
            String thisSubtypeNoSuffix = getSubtype().substring(0, thisPlusIdx);
            String thisSubtypeSuffix = getSubtype().substring(thisPlusIdx + 1);
            String otherSubtypeSuffix = other.getSubtype().substring(otherPlusIdx + 1);
            if (thisSubtypeSuffix.equals(otherSubtypeSuffix) && WILDCARD_TYPE.equals(thisSubtypeNoSuffix)) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(this.type);
    builder.append('/');
    builder.append(this.subtype);
    parameters.forEach((key, value) -> builder.append(';').append(key).append('=').append(value));
    return builder.toString();
  }

  // comparison by specificity
  // audio/toto+mp3 < audio/mp3 < audio/* < */*
  @Override
  public int compareTo(@NotNull ContentType o) {
    if (isWildcardType() && !o.isWildcardType()) {
      return 1;
    } else if (o.isWildcardType() && !isWildcardType()) {
      return -1;
    }
    int typeComparison = getType().compareTo(o.getType());
    if (typeComparison != 0) {
      return typeComparison;
    }
    if (isWildcardSubtype() && !o.isWildcardSubtype()) {
      return 1;
    } else if (o.isWildcardSubtype() && !isWildcardSubtype()) {
      return -1;
    }
    int subTypeComparisonPrefix = getSubtypeSuffix().orElse(getSubtype()).compareTo(o.getSubtypeSuffix().orElse(o.getSubtype()));
    if (subTypeComparisonPrefix != 0) {
      return subTypeComparisonPrefix;
    }
    if (getSubtypeSuffix().isPresent() && !o.getSubtypeSuffix().isPresent()) {
      return -1;
    } else if (o.getSubtypeSuffix().isPresent() && !getSubtypeSuffix().isPresent()) {
      return 1;
    }
    int subTypeComparison = getSubtype().compareTo(o.getSubtype());
    if (subTypeComparison != 0) {
      return subTypeComparison;
    }
    return Integer.compare(getParameters().size(), o.getParameters().size());
  }
}
