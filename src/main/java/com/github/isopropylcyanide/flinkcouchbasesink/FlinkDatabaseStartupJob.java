/*
 * Licensed under the Apache License, Version 2.0 (the "License");	 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.	 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	 * You may obtain a copy of the License at
 *	 *
 * http://www.apache.org/licenses/LICENSE-2.0	 * http://www.apache.org/licenses/LICENSE-2.0
 *	 *
 * Unless required by applicable law or agreed to in writing, software	 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,	 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.	 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and	 * See the License for the specific language governing permissions and
 * limitations under the License.	 * limitations under the License.
 */
package com.github.isopropylcyanide.flinkcouchbasesink;

import com.couchbase.client.deps.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.java.io.TextInputFormat;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.FileProcessingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

public class FlinkDatabaseStartupJob {

    private static final Logger log = LoggerFactory.getLogger(FlinkDatabaseStartupJob.class);

    public static void main(String[] args) {
        try {
            StreamExecutionEnvironment environment = StreamingEnvironment.instance.getExecutionEnv();
            Properties properties = new Properties();
            properties.load(FlinkDatabaseStartupJob.class.getResourceAsStream("/config.properties"));


            final String jsonStarterPath = properties.getProperty(JobProperties.DOCUMENTS_PATH);
            Path path = new Path(jsonStarterPath);

            FileProcessingMode fileProcessingMode = FileProcessingMode.PROCESS_ONCE;
            long fileReadPollDuration = 0;
            Boolean isFilePollEnabled = Boolean.valueOf(properties.getProperty(JobProperties.STARTUP_DOCUMENT_POLL_CONTINUOUS));

            if (isFilePollEnabled) {
                fileReadPollDuration = Long.parseLong(properties.getProperty(JobProperties.STARTUP_DOCUMENTS_POLL_DURATION));
                fileProcessingMode = FileProcessingMode.PROCESS_CONTINUOUSLY;
            }

            SingleOutputStreamOperator<List<SinkJsonDocument>> jsonDocumentStream = environment
                    .readFile(new UntilEOFTextInputFormat(path), jsonStarterPath, fileProcessingMode, fileReadPollDuration)
                    .map(FlinkDatabaseStartupJob::acceptJsonStringAsDocument)
                    .filter(Objects::nonNull);

            jsonDocumentStream.addSink(new CouchbaseJsonDocumentSink(properties)).name("Couchbase Json Document Sink");
            environment.execute("Flink Couchbase Json Sink Job");

        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private static List<SinkJsonDocument> acceptJsonStringAsDocument(String jsonDocString) {
        try {
            return new ObjectMapper().readValue(jsonDocString, SinkJsonDocument.getStarterJsonDocumentTypeReference());

        } catch (Exception ex) {
            log.error(ex.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * A custom implementation of text input that delimits by EOF and not the default line feed
     */
    private static class UntilEOFTextInputFormat extends TextInputFormat {

        private UntilEOFTextInputFormat(Path filePath) {
            super(filePath);
            super.setDelimiter("0xff");
        }

        @Override
        public String readRecord(String reusable, byte[] bytes, int offset, int numBytes) {
            log.info("Scheduled read [Database starter]: {} ", Date.from(Instant.now()));
            return new String(bytes, offset, numBytes);
        }
    }
}
