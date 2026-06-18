package ru.berkut.spring.hubflow.exception;

public class AccessDeniedException extends RuntimeException {
    public AccessDeniedException(String message) { super(message); }
    public AccessDeniedException() { super("Access denied"); }
}
