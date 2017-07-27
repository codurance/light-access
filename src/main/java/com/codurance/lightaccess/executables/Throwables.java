package com.codurance.lightaccess.executables;

import java.util.concurrent.Callable;

public class Throwables {

    @FunctionalInterface
    public interface Command {
        void execute() throws Exception;
    }

    @FunctionalInterface
    public interface Query<T> extends Callable<T> {
    }

    @FunctionalInterface
    public interface ExceptionWrapper<E> {
        E wrap(Exception e);
    }

    //TODO Rename execute and executeQuery to "try"
    public static void execute(Command command) throws RuntimeException {
        execute(command, RuntimeException::new);
    }

    public static <E extends Throwable> void execute(Command command, ExceptionWrapper<E> wrapper) throws E {
        try {
            command.execute();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw wrapper.wrap(e);
        }
    }

    public static <T> T executeQuery(Query<T> callable) throws RuntimeException {
        return executeQuery(callable, RuntimeException::new);
    }

    public static <T, E extends Throwable> T executeQuery(Callable<T> callable, ExceptionWrapper<E> wrapper) throws E {
        try {
            return callable.call();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw wrapper.wrap(e);
        }
    }

    public static <T extends AutoCloseable> void executeWithResource(T closeableResource, Command command) {
        try(T ignored = closeableResource) {
            command.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <R, T extends AutoCloseable> R executeWithResource(T closeableResource, Query<R> callable) {
        try(T ignored = closeableResource) {
            return callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



}
