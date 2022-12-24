module com.github.wnameless.json.flattener {
  requires transitive com.github.wnameless.json.base;
  requires transitive org.apache.commons.text;
  requires org.apache.commons.lang3;
  requires com.fasterxml.jackson.core;
  requires com.fasterxml.jackson.databind;

  exports com.github.wnameless.json.flattener;
  exports com.github.wnameless.json.unflattener;
}
