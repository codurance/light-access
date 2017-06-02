package com.codurance.lightaccess;

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
}
