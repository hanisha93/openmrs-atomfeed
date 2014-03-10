package org.bahmni.module.openerpatomfeedclient.api.client.impl;

import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.ict4h.atomfeed.jdbc.JdbcConnectionProvider;
import org.ict4h.atomfeed.transaction.AFTransactionManager;
import org.ict4h.atomfeed.transaction.AFTransactionWork;
import org.openmrs.api.context.ServiceContext;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class AtomFeedSpringTransactionManager implements AFTransactionManager, JdbcConnectionProvider {
    private PlatformTransactionManager transactionManager;
    private Map<AFTransactionWork.PropagationDefinition, Integer> propagationMap = new HashMap<AFTransactionWork.PropagationDefinition, Integer>();

    public AtomFeedSpringTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
        propagationMap.put(AFTransactionWork.PropagationDefinition.PROPAGATION_REQUIRED, TransactionDefinition.PROPAGATION_REQUIRED);
        propagationMap.put(AFTransactionWork.PropagationDefinition.PROPAGATION_REQUIRES_NEW, TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public <T> T executeWithTransaction(final AFTransactionWork<T> action) throws RuntimeException {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        Integer txPropagationDef = getTxPropagation(action.getTxPropagationDefinition());
        transactionTemplate.setPropagationBehavior(txPropagationDef);
        return transactionTemplate.execute( new TransactionCallback<T>() {
            @Override
            public T doInTransaction(TransactionStatus status) {
                return action.execute();
            }
        });
    }

    private Integer getTxPropagation(AFTransactionWork.PropagationDefinition propagationDefinition) {
        return propagationMap.get(propagationDefinition);
    }

    /**
     * @see org.ict4h.atomfeed.jdbc.JdbcConnectionProvider
     * @return
     * @throws SQLException
     */
    @Override
    public Connection getConnection() throws SQLException {
        //TODO: ensure that only connection associated with current thread current transaction is given
        return getSession().connection();
    }

    private Session getSession() {
        ServiceContext serviceContext = ServiceContext.getInstance();
        Class klass = serviceContext.getClass();
        try {
            Field field = klass.getDeclaredField("applicationContext");
            field.setAccessible(true);
            ApplicationContext applicationContext = (ApplicationContext) field.get(serviceContext);
            SessionFactory factory = (SessionFactory) applicationContext.getBean("sessionFactory");
            return factory.getCurrentSession();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see org.ict4h.atomfeed.jdbc.JdbcConnectionProvider
     * @param connection
     * @throws SQLException
     */
    @Override
    public void closeConnection(Connection connection) throws SQLException {
        System.out.println("Close Connection called. This should not happen");
    }


    /**
     *  @see org.ict4h.atomfeed.jdbc.JdbcConnectionProvider
     */
    @Override
    public void startTransaction() {
        System.out.println("Start Tx called. This should not happen");
    }

    /**
     *  @see org.ict4h.atomfeed.jdbc.JdbcConnectionProvider
     */
    @Override
    public void commit() {
        System.out.println("Commit called. This should not happen");
    }

    /**
     *  @see org.ict4h.atomfeed.jdbc.JdbcConnectionProvider
     */
    @Override
    public void rollback() {
        System.out.println("Rollback called. This should not happen");
    }

}