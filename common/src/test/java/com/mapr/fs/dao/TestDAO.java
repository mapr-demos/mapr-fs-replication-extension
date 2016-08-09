package com.mapr.fs.dao;

import java.io.IOException;

public class TestDAO extends AbstractDAO {

  private static final String TYPE = "test";
  private static final String SPL_TABLE_NAME = "table";

  private static String fullPath;

  public TestDAO() throws IOException {
    fullPath = this.getFullTableName(TYPE, SPL_TABLE_NAME);
  }

  public String getFullPath() {
    return fullPath;
  }
}
