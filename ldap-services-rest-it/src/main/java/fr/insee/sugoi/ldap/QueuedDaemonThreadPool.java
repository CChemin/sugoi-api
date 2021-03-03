package fr.insee.sugoi.ldap;

import wiremock.org.eclipse.jetty.util.thread.QueuedThreadPool;

public class QueuedDaemonThreadPool extends QueuedThreadPool {



  public QueuedDaemonThreadPool(int maxThreads) {
    super(maxThreads);
  }

  @Override
  public boolean isDaemon() {
    return true;
  }



}
