package com.tambapps.http.hyperpoet;

import lombok.Data;

// TODO document this
@Data
public class FormPart {

  // optional
  String filename;
  Object value;
  ContentType contentType;

}
