package com.ares.concurrency.pooled.example;

import com.ares.concurrency.pooled.PooledFactory;

public class DbConnectionFactory implements PooledFactory<MockDbConnection> {

  @Override
  public MockDbConnection create() throws Exception {
    return new MockDbConnection();
  }

  @Override
  public void destroy(MockDbConnection obj) {
    if (obj != null) {
      obj.close();
    }
  }

  @Override
  public boolean validate(MockDbConnection obj) {
    // 在借出和归还时检查连接是否“存活”
    return obj != null &&  !obj.isClosed();
  }
}
