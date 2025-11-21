module com.github.wnameless.json.flattener {
  requires transitive com.github.wnameless.json.base;
  requires transitive org.apache.commons.text;
  requires org.apache.commons.lang3;
  requires tools.jackson.databind;

  exports com.github.wnameless.json.flattener;
  exports com.github.wnameless.json.unflattener;
}
