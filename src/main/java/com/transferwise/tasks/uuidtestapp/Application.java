package com.transferwise.tasks.uuidtestapp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.system.ApplicationPidFileWriter;

@SpringBootApplication
@Slf4j
public class Application {
	public static void main(String[] args) {
		try {
			Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandlerImpl());

			SpringApplication springApplication = new SpringApplication(Application.class);
			springApplication.addListeners(new ApplicationPidFileWriter());
			springApplication.run(args);
		} catch (Throwable t) {
			if (!t.getClass().getSimpleName().equals("SilentExitException")) {
				log.error(t.getMessage(), t);
			}
			throw t;
		}
	}

	static class UncaughtExceptionHandlerImpl implements Thread.UncaughtExceptionHandler {
		@Override
		public void uncaughtException(Thread thread, Throwable throwable) {
			log.error("Uncaught exception occured in '" + thread.getId() + "'. Contact developers immediately.", throwable);
			throwable.printStackTrace(); // In case logging system is messed up.
		}
	}
}
