package com.transferwise.tasks.uuidtestapp;

import com.transferwise.tasks.uuidtestapp.resource.CachedResourceService;
import com.transferwise.tasks.uuidtestapp.resource.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Random;

@Component
public class ResourceClients {
	private static final int CLIENTS_COUNT = 10;

	@Autowired
	CachedResourceService cachedResourceService;
	@Autowired
	ResourceService resourceService;

	@PostConstruct
	public void doSomeMagic() {
		Thread[] threads = new Thread[CLIENTS_COUNT];
		ClientState[] clientStates = new ClientState[CLIENTS_COUNT];

		for (int i = 0; i < CLIENTS_COUNT; i++) {
			int finalI = i;
			ClientState clientState = clientStates[i] = new ClientState();
			threads[i] = new Thread(() -> {
				for (int j = 0; j < 10; j++) {
					sleep(2000 + new Random().nextInt(6000));

					clientState.startTimeMs = System.currentTimeMillis();
					String resource = cachedResourceService.getCachedResource(1l);
					clientState.resource = resource;
					clientState.startTimeMs = -1;
				}
			});
		}

		for (Thread thread : threads) {
			thread.start();
		}

		new Thread(() -> {
			while (true) {
				System.out.print("\033[H\033[2J");
				System.out.println("Resource:");
				System.out.println("   Queries: " + resourceService.getConcurrency());
				System.out.println("   Value: " + resourceService.getResourceValue());
				System.out.println();
				for (int i = 0; i < CLIENTS_COUNT; i++) {
					ClientState clientState = clientStates[i];
					long startTimeMs = clientState.startTimeMs;
					if (startTimeMs != -1) {
						System.out.print("\u001B[32m");
					}
					System.out.print(i);
					System.out.print(" - ");
					System.out.print(clientState.resource == null ? "<NO_VALUE>" : clientState.resource);
					System.out.print(" ");
					if (startTimeMs != -1) {
						int timeTakenS = (int) ((System.currentTimeMillis() - startTimeMs) / 1000);
						for (int j = 0; j < timeTakenS; j++) {
							System.out.print(".");
						}
						System.out.print("\u001B[0m");
					}
					System.out.println();
				}
				sleep(50);
			}
		}).start();

		for (Thread thread : threads) {
			join(thread);
		}
	}

	private void sleep(long timeMs) {
		try {
			Thread.sleep(timeMs);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void join(Thread thread) {
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private class ClientState {
		long startTimeMs = -1;
		String resource;
	}
}
