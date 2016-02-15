package ro.cosu.vampires.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

import ro.cosu.vampires.server.writers.json.JsonResultsWriter;

public class Data {


    private static final Logger LOG = LoggerFactory.getLogger(Data.class);


    private static void loadFromJson() throws FileNotFoundException {
        Gson gson = new GsonBuilder().setPrettyPrinting()
                                     .registerTypeAdapter(LocalDateTime.class, new JsonResultsWriter.LocalDateTimeSerializer())
                                     .create();
        JsonReader jsonReader = new JsonReader(new FileReader("jsonFile.json"));

    }

    public static void main(String[] args) throws IOException {
        List<File> results = Files.list(Paths.get(System.getProperty("user.dir")))
                .filter(p -> !p.getFileName()
                        .toString().startsWith("."))
                .filter(p -> !p.toFile().isDirectory())
                .limit(1).map(Path::toFile)
                .peek(f -> LOG.info("{}", f))
                .collect(Collectors.toList());


        String logFile = results.get(0).getAbsoluteFile().toString(); // Should be some file on your system
        SparkConf conf = new SparkConf().setAppName("Simple Application").setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(conf);
        JavaRDD<String> logData = sc.textFile(logFile).cache();



        long numAs = logData.filter((Function<String, Boolean>) s -> s.contains("a")).count();

        long numBs = logData.filter((Function<String, Boolean>) s -> s.contains("b")).count();


        LOG.info("Lines with a: " + numAs + ", lines with b: " + numBs);
    }
}
