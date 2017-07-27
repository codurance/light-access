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
import static org.mockito.Mockito.*;

public class ThrowablesShould {

    private final Throwables throwables = new Throwables();
    @Rule
    public MockitoRule mockito = MockitoJUnit.rule();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private Command command;

    @Mock
    private Throwables.Query query;

    @Test
    public void executeCommand() throws Exception {
        throwables.execute(command);

        verify(command).execute();
    }

    @Test
    public void throwsRuntimeExceptionThrownByCommand() throws Exception {
        doThrow(new RuntimeException("I throw runtimeException")).when(command).execute();
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage(is("I throw runtimeException"));

        throwables.execute(command);
    }

    @Test
    public void wrapsCheckedExceptionThrownByCommandWithRuntimeException() throws Exception {
        Exception exceptionThrownByCommand = new Exception("I throw checked exception");
        doThrow(exceptionThrownByCommand).when(command).execute();
        expectedException.expect(RuntimeException.class);
        expectedException.expectCause(is(exceptionThrownByCommand));

        throwables.execute(command);
    }

    @Test
    public void throwsRuntimeExceptionThrownByCommandWithoutWrapping() throws Exception {
        doThrow(new RuntimeException("I throw runtimeException")).when(command).execute();
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage(is("I throw runtimeException"));

        throwables.execute(command, null);
    }

    @Test
    public void wrapsCheckedExceptionThrownByCommandWithExceptionDefinedByWrapper() throws Exception {
        Exception exceptionThrownByCommand = new Exception("I throw checked exception");
        doThrow(exceptionThrownByCommand).when(command).execute();
        expectedException.expect(TestException.class);
        expectedException.expectCause(is(exceptionThrownByCommand));

        throwables.execute(command, TestException::new);
    }

    @Test
    public void executeQuery() throws Exception {
        throwables.executeQuery(query);

        verify(query).call();
    }

    @Test
    public void throwsRuntimeExceptionThrownByQuery() throws Exception {
        doThrow(new RuntimeException("I throw runtimeException")).when(query).call();
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage(is("I throw runtimeException"));

        throwables.executeQuery(query);
    }

    @Test
    public void wrapsCheckedExceptionThrownByQueryWithRuntimeException() throws Exception {
        Exception exceptionThrownByCommand = new Exception("I throw checked exception");
        doThrow(exceptionThrownByCommand).when(query).call();
        expectedException.expect(RuntimeException.class);
        expectedException.expectCause(is(exceptionThrownByCommand));

        throwables.executeQuery(query);
    }

    @Test
    public void throwsRuntimeExceptionThrownByQueryWithoutWrapping() throws Exception {
        doThrow(new RuntimeException("I throw runtimeException")).when(query).call();
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage(is("I throw runtimeException"));

        throwables.executeQuery(query, null);
    }

    @Test
    public void wrapsCheckedExceptionThrownByQueryWithExceptionDefinedByWrapper() throws Exception {
        Exception exceptionThrownByCommand = new Exception("I throw checked exception");
        doThrow(exceptionThrownByCommand).when(query).call();
        expectedException.expect(TestException.class);
        expectedException.expectCause(is(exceptionThrownByCommand));

        throwables.executeQuery(query, TestException::new);
    }

    private class TestException extends Exception {
        public TestException(Exception e) {
            super(e);
        }
    }
}
