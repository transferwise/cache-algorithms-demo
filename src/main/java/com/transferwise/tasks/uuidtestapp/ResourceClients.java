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

	private volatile boolean shuttingDown;

	@PostConstruct
	public void doSomeMagic() {
		Thread[] threads = new Thread[CLIENTS_COUNT];
		ClientState[] clientStates = new ClientState[CLIENTS_COUNT];

		for (int i = 0; i < CLIENTS_COUNT; i++) {
			int finalI = i;
			ClientState clientState = clientStates[i] = new ClientState();
			threads[i] = new Thread(() -> {
				for (int j = 0; j < 100; j++) {
					sleep(2000 + new Random().nextInt(6000));

					clientState.startQuerying();
					try {
						String resource = cachedResourceService.getCachedResource(1l);
						clientState.resourceValue = resource;
					}
					finally {
						clientState.stopQuerying();
					}
				}
			});
		}

		for (Thread thread : threads) {
			thread.start();
		}

		new Thread(() -> {
			while (!shuttingDown) {
				String currentCachedResource = cachedResourceService.getCurrentCachedResource(1l);
				if (currentCachedResource == null){
					currentCachedResource = "<NO_VALUE>";
				}

				ansiClear();
				System.out.println("Resource:");
				System.out.println("   Queries: " + resourceService.getConcurrency() + " (max " + resourceService.getMaxConcurrency() + ")");
				System.out.println("   Value in Resource: " + resourceService.getResourceValue());
				System.out.println("   Cached value: " + currentCachedResource);
				System.out.println();
				for (int i = 0; i < CLIENTS_COUNT; i++) {
					ClientState clientState = clientStates[i];
					long queryStartTimeMs = clientState.queryStartTimeMs;
					long timeTakenMs = queryStartTimeMs == -1 ? -1 : System.currentTimeMillis() - queryStartTimeMs;

					boolean querying = clientState.querying;
					if (querying) {
						ansiGreen();
					} else if ((timeTakenMs != -1) && (timeTakenMs < 2000)) {
						ansiYellow();
					}
					System.out.print("#");
					System.out.print(i);
					System.out.print(" - ");
					System.out.print(clientState.resourceValue);
					System.out.print(" ");
					if (querying) {
						int timeTakenS = (int) (timeTakenMs / 1000);
						for (int j = 0; j < timeTakenS; j++) {
							System.out.print(".");
						}
					}
					ansiDefaultColor();
					System.out.println();
				}

				System.out.println();
				ansiYellow();
				System.out.println("YELLOW - Immediate answer to a query");
				ansiGreen();
				System.out.println("GREEN - Doing Query, one dot = 1 second");
				ansiDefaultColor();
				sleep(50);
			}
		}).start();

		for (Thread thread : threads) {
			join(thread);
		}

		shuttingDown = true;
	}

	private void ansiClear(){
		System.out.print("\033[H\033[2J");
	}

	private void ansiGreen(){
		System.out.print("\u001B[32m");
	}

	private void ansiYellow(){
		System.out.print("\u001B[33m");
	}

	private void ansiDefaultColor(){
		System.out.print("\u001B[0m");
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
		volatile boolean querying;
		volatile long queryStartTimeMs = -1;
		volatile String resourceValue = "<NO_VALUE>";

		public void startQuerying() {
			querying = true;
			queryStartTimeMs = System.currentTimeMillis();
		}

		public void stopQuerying() {
			querying = false;
		}
	}
}
