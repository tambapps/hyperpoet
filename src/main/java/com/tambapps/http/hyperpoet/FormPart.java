package com.tambapps.http.hyperpoet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// TODO document this
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormPart {

  // optional
  String filename;
  Object value;
  ContentType contentType;

}
