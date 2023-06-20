package com.tambapps.http.hyperpoet;

import com.tambapps.http.contenttype.ContentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Class representing a part of a Multipart form request body
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormPart {

  // optional
  String filename;
  Object value;
  ContentType contentType;

}
