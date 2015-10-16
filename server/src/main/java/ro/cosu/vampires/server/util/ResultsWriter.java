package ro.cosu.vampires.server.util;

import ro.cosu.vampires.server.Message;

import java.util.List;

public interface ResultsWriter{
    void writeResults(List<Message.Result> results);

}
