Minimal reproduction of a difference when using Application Insights in Spring Boot 3.5.x and 4.x.
To run the example:

1. Go to the `dockercompose` directory and run the Kafka cluster using `docker compose up`.
2. Paste a valid connection string into the `src/main/resources/applicationinsights.json` file.
3. Run the demo application and note the console.
4. Switch to the `spring-boot-4.x` branch
5. Run the demo application again and note the console.

Expected behavior:
Every 5 seconds a new log line is added saying `Traceparent: 00-f26e79186c5b4f3e9821f4f9c85a3d88-139e06eee11d26ac-01. Trace id: f26e79186c5b4f3e9821f4f9c85a3d88.`
This confirms that the OpenTelemetry span has been created based on the `traceparent` header.

Actual behavior:
When running with Spring Boot 4.x the logs contain `Traceparent: 00-79200f8e11aeb85c63ebeb6626ade0bb-9a14e9f0dbac4803-01. Trace id: 00000000000000000000000000000000.` instead.