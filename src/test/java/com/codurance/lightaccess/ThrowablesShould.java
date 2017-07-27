package com.codurance.lightaccess;

import com.codurance.lightaccess.executables.Throwables;
import com.codurance.lightaccess.executables.Throwables.Command;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

public class ThrowablesShould {

    private final Throwables throwables = new Throwables();

    @Rule public MockitoRule mockito = MockitoJUnit.rule();

    @Rule public ExpectedException expectedException = ExpectedException.none();

    @Mock Command command;
    @Mock Throwables.Query query;
    @Mock AutoCloseable resource;

    @Test public void
    execute_command() throws Exception {
        Throwables.execute(command);

        verify(command).execute();
    }

    @Test public void
    throws_command_runtime_exception() throws Exception {
        doThrow(new DummyRuntimeException("exception message")).when(command).execute();

        expectedException.expect(DummyRuntimeException.class);
        expectedException.expectMessage(is("exception message"));

        Throwables.execute(command);
    }

    @Test public void
    wraps_checked_exception_thrown_by_command_with_runtime_wrapper() throws Exception {
        DummyCheckedException dummyCheckedException = new DummyCheckedException();
        doThrow(dummyCheckedException).when(command).execute();

        expectedException.expect(RuntimeException.class);
        expectedException.expectCause(is(dummyCheckedException));

        Throwables.execute(command);
    }

    @Test public void
    wraps_runtime_checked_exception_thrown_by_command_with_another_checked_exception() throws Exception {
        Exception commandException = new Exception();
        doThrow(commandException).when(command).execute();

        expectedException.expect(DummyCheckedException.class);
        expectedException.expectCause(is(commandException));

        Throwables.execute(command, DummyCheckedException::new);
    }

    @Test public void
    execute_query() throws Exception {
        Throwables.executeQuery(query);

        verify(query).call();
    }

    @Test public void
    throws_query_runtime_exception() throws Exception {
        doThrow(new DummyRuntimeException("exception message")).when(query).call();

        expectedException.expect(DummyRuntimeException.class);
        expectedException.expectMessage(is("exception message"));

        Throwables.executeQuery(query);
    }

    @Test public void
    wraps_checked_exception_thrown_by_query_with_runtime_wrapper() throws Exception {
        DummyCheckedException dummyCheckedException = new DummyCheckedException();
        doThrow(dummyCheckedException).when(query).call();

        expectedException.expect(RuntimeException.class);
        expectedException.expectCause(is(dummyCheckedException));

        Throwables.executeQuery(query);
    }

    @Test public void
    wraps_runtime_checked_exception_thrown_by_query_with_another_checked_exception() throws Exception {
        Exception queryException = new Exception();
        doThrow(queryException).when(query).call();

        expectedException.expect(DummyCheckedException.class);
        expectedException.expectCause(is(queryException));

        Throwables.executeQuery(query, DummyCheckedException::new);
    }

    @Test public void
    close_resource_when_executing_a_command() throws Exception {
        Throwables.executeWithResource(resource, command);
        
        verify(resource).close();
    }

    @Test public void
    close_resource_even_when_a_command_throws_exception() throws Exception {
        DummyCheckedException commandException = new DummyCheckedException();
        doThrow(commandException).when(command).execute();

        expectedException.expect(RuntimeException.class);
        expectedException.expectCause(is(commandException));
        
        Throwables.executeWithResource(resource, command);

        verify(resource).close();
    }

    @Test public void
    close_resource_when_executing_a_query() throws Exception {
        Throwables.executeWithResource(resource, query);

        verify(resource).close();
    }

    @Test public void
    close_resource_even_when_a_query_throws_exception() throws Exception {
        DummyCheckedException queryException = new DummyCheckedException();
        doThrow(queryException).when(query).call();

        expectedException.expect(RuntimeException.class);
        expectedException.expectCause(is(queryException));

        Throwables.executeWithResource(resource, query);

        verify(resource).close();
    }

    private class DummyRuntimeException extends RuntimeException {
        DummyRuntimeException(String message) {
            super(message);
        }
    }

    private class DummyCheckedException extends Exception {
        DummyCheckedException(Exception e) {
            super(e);
        }

        DummyCheckedException() {
            super();
        }
    }


}
