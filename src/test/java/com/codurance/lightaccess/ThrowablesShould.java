package com.codurance.lightaccess;

import com.codurance.lightaccess.Throwables.Command;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;

public class ThrowablesShould {

    @Rule
    public MockitoRule mockito = MockitoJUnit.rule();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private Command command;

    @Test
    public void executeCommand() throws Exception {
        Throwables throwables = new Throwables();

        throwables.execute(command);

        verify(command).execute();
    }

    @Test
    public void throwsRuntimeExceptionThrownByCommand() throws Exception {
        Throwables throwables = new Throwables();
        doThrow(new RuntimeException("I throw runtimeException")).when(command).execute();
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage(is("I throw runtimeException"));

        throwables.execute(command);
    }

    @Test
    public void wrapsCheckedExceptionThrownByCommandWithRuntimeException() throws Exception {
        Throwables throwables = new Throwables();
        Exception exceptionThrownByCommand = new Exception("I throw checked exception");
        doThrow(exceptionThrownByCommand).when(command).execute();
        expectedException.expect(RuntimeException.class);
        expectedException.expectCause(is(exceptionThrownByCommand));

        throwables.execute(command);
    }

    @Test
    public void throwsRuntimeExceptionThrownByCommandWithoutWrapping() throws Exception {
        Throwables throwables = new Throwables();
        doThrow(new RuntimeException("I throw runtimeException")).when(command).execute();
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage(is("I throw runtimeException"));

        throwables.execute(command, null);
    }

    @Test
    public void wrapsCheckedExceptionThrownByCommandWithExceptionDefinedByWrapper() throws Exception {
        Throwables throwables = new Throwables();
        Exception exceptionThrownByCommand = new Exception("I throw checked exception");
        doThrow(exceptionThrownByCommand).when(command).execute();
        expectedException.expect(TestException.class);
        expectedException.expectCause(is(exceptionThrownByCommand));

        throwables.execute(command, TestException::new);
    }

    private class TestException extends Exception {
        public TestException(Exception e) {
            super(e);
        }
    }
}
