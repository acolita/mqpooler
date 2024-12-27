package br.com.acolita.consumer;

import java.util.Objects;

public class QueueDefinition {
    private final String requestQueue;
    private final String responseQueue;

    public QueueDefinition(String requestQueue, String responseQueue) {
        this.requestQueue = requestQueue;
        this.responseQueue = responseQueue;
    }

    public String getRequestQueue() {
        return requestQueue;
    }

    public String getResponseQueue() {
        return responseQueue;
    }

    public static QueueDefinition of(String requestQueue, String responseQueue) {
        return new QueueDefinition(requestQueue, responseQueue);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QueueDefinition that = (QueueDefinition) o;
        return Objects.equals(requestQueue, that.requestQueue) &&
                Objects.equals(responseQueue, that.responseQueue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestQueue, responseQueue);
    }

    @Override
    public String toString() {
        return "QueueDefinition{" +
                "requestQueue='" + requestQueue + '\'' +
                ", responseQueue='" + responseQueue + '\'' +
                '}';
    }
}
