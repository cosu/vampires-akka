/*
 *
 *  * The MIT License (MIT)
 *  * Copyright © 2016 Cosmin Dumitru, http://cosu.ro <cosu@cosu.ro>
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the “Software”), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in
 *  * all copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  * THE SOFTWARE.
 *  *
 *
 */

package ro.cosu.vampires.server.values.jobs;


import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.io.Files;

import com.typesafe.config.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class JobUtil {
    private static final Logger LOG = LoggerFactory.getLogger(JobUtil.class);


    public static List<Job> fromConfig(Config config) {
        String format = "";
        if (config.hasPath("format")) {
            format = config.getString("format");
        }

        String url = "";
        if (config.hasPath("url")) {
            url = config.getString("url");
        }

        int sequenceStart = config.getInt("sequenceStart");
        int sequenceStop = config.getInt("sequenceStop");
        String task = config.getString("task");


        final String finalUrl = url;
        final String finalFormat = format;
        List<Job> jobs = IntStream.rangeClosed(sequenceStart, sequenceStop).mapToObj(i -> String.format(finalFormat, i))
                .map(f -> String.format("%s %s%s", task, finalUrl, f).trim())
                .map(command -> Job.empty().withCommand(command))
                .collect(Collectors.toList());

        LOG.info("Build bag from params:{}:{}:{}:{}-{} - size {}", task, url, format, sequenceStart,
                sequenceStop, jobs.size());

        return jobs;
    }

    public static List<Job> bagFromFile(String id, File file) {

        Preconditions.checkArgument(file.exists(), "File does not exist");
        Preconditions.checkArgument(file.isFile(), "File is a directory");

        List<Job> jobs;
        try {
            List<String> lines;
            lines = Files.readLines(file, Charsets.UTF_8);

            jobs = lines.stream().filter(l -> !l.isEmpty()).map(l -> Job.empty().withCommand(l))
                    .collect(Collectors.toList());

        } catch (IOException e) {
            LOG.error("failed to read tasks from file", e);
            throw new IllegalArgumentException("failed to read tasks from file ", e);
        }
        LOG.debug(String.format("bag-id: %1s, read %2s jobs from %3s", id, jobs.size(), file.getName()));
        return jobs;
    }


}