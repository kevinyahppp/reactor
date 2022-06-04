package com.ideas.reactor.app;

import com.ideas.reactor.app.models.Comments;
import com.ideas.reactor.app.models.User;
import com.ideas.reactor.app.models.UserWithComments;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

@SpringBootApplication
@Slf4j
public class AppApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(AppApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		log.info("===========> iterableExample");
		iterableExample();
		log.info("===========> flatMapExample");
		flatMapExample();
		log.info("===========> convertToStringExample");
		convertToStringExample();
		log.info("===========> collectListExample");
		collectListExample();
		log.info("===========> userCommentsFlatMapExample");
		userCommentsFlatMapExample();
		log.info("===========> userCommentsZipWithExample");
		userCommentsZipWithExample();
		log.info("===========> userCommentsZipWithExample2");
		userCommentsZipWithExample2();
		log.info("===========> zipWithRange");
		zipWithRange();
//		log.info("===========> intervalExample");
//		intervalExample();
//		log.info("===========> delayExample");
//		delayExample();
//		log.info("===========> infiniteIntervalExample");
//		infiniteIntervalExample();
//		log.info("===========> intervalFromCreateExample");
//		intervalFromCreateExample();
		log.info("===========> backPressureExample");
		backPressureExample();
	}

	public void backPressureExample() {
		Flux.range(1, 10)
				.log()
				//.limitRate(2)
				.subscribe(
				new Subscriber<Integer>() {
					private Subscription subscription;
					private Integer limit = 2;
					private Integer consume = 0;
					@Override
					public void onSubscribe(Subscription subscription) {
						this.subscription = subscription;
						subscription.request(limit);
					}

					@Override
					public void onNext(Integer integer) {
						log.info(integer.toString());
						consume++;
						if (consume == limit) {
							consume = 0;
							subscription.request(limit);
						}
					}

					@Override
					public void onError(Throwable throwable) {

					}

					@Override
					public void onComplete() {

					}
				}
			);
	}

	public void intervalFromCreateExample() {
		Flux.create(emitter -> {
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				private Integer counter = 0;
				@Override
				public void run() {
					emitter.next(++counter);
					if (counter == 10) {
						timer.cancel();
						emitter.complete();
					}
				}
			}, 1000, 1000);
		}).subscribe(next -> log.info(next.toString()),
				error -> log.error(error.getMessage()),
				() -> log.info("Task completed"));
	}

	public void infiniteIntervalExample() throws InterruptedException {
		CountDownLatch countDownLatch = new CountDownLatch(1);

		Flux.interval(Duration.ofSeconds(1))
				.doOnTerminate(() -> countDownLatch.countDown())
				.flatMap(i -> {
					if(i >= 5) {
						return Flux.error(new InterruptedException("Just until 5"));
					}
					return Flux.just(i);
				})
				.map(i -> "Hi number ".concat(i.toString()))
				.retry(2)
				.subscribe(log::info, e -> log.error(e.getMessage()));

		countDownLatch.await();
	}

	public void delayExample() {
		Flux<Integer> range = Flux.range(1, 12)
				.delayElements(Duration.ofSeconds(1))
				.doOnNext(i -> log.info(i.toString()));
		range.blockLast();
	}
	public void intervalExample() {
		Flux<Integer> range = Flux.range(1, 12);
		Flux<Long> delay = Flux.interval(Duration.ofSeconds(1));

		range.zipWith(delay, (ra, de) -> ra)
				.doOnNext(i -> log.info(i.toString()))
				.blockLast();
	}

	public void zipWithRange() {
		Flux.just(1, 2, 3, 4)
				.map(i -> (i * 2))
				.zipWith(Flux.range(0, 4),
						(one, two) -> String.format("First Flux: %d, Second Flux: %d", one, two))
				.subscribe(txt -> log.info(txt));
	}

	public void userCommentsZipWithExample2() {
		Mono<User> userMono = Mono.fromCallable(() -> new User("Kevin", "Garcia"));
		Mono<Comments> commentsMono = Mono.fromCallable(() -> {
			Comments comments = new Comments();
			comments.addComment("Comment1");
			comments.addComment("Comment2");
			comments.addComment("Comment3");
			return comments;
		});

		userMono.zipWith(commentsMono)
				.map(tuple -> {
					User user = tuple.getT1();
					Comments comments = tuple.getT2();
					return new UserWithComments(user, comments);
				})
				.subscribe(userWithComments -> log.info(userWithComments.toString()));
	}

	public void userCommentsZipWithExample() {
		Mono<User> userMono = Mono.fromCallable(() -> new User("Kevin", "Garcia"));
		Mono<Comments> commentsMono = Mono.fromCallable(() -> {
			Comments comments = new Comments();
			comments.addComment("Comment1");
			comments.addComment("Comment2");
			comments.addComment("Comment3");
			return comments;
		});

		userMono.zipWith(commentsMono, (user, comments) -> new UserWithComments(user, comments))
				.subscribe(userWithComments -> log.info(userWithComments.toString()));
	}

	public void userCommentsFlatMapExample() {
		Mono<User> userMono = Mono.fromCallable(() -> new User("Kevin", "Garcia"));
		Mono<Comments> commentsMono = Mono.fromCallable(() -> {
			Comments comments = new Comments();
			comments.addComment("Comment1");
			comments.addComment("Comment2");
			comments.addComment("Comment3");
			return comments;
		});

		userMono.flatMap(user -> commentsMono.map(comments -> new UserWithComments(user, comments)))
				.subscribe(userWithComments -> log.info(userWithComments.toString()));
	}

	public void collectListExample() {
		List<User> usersList = Arrays.asList(new User("Tom", "Cruise"),
				new User("Emilia", "Clark"),
				new User("Brad", "Pitt"),
				new User("Bruce", "Wills"),
				new User("Bruce", "Lee"));
		Flux.fromIterable(usersList)
				.collectList()
				.subscribe(userList -> {userList.stream().forEach(user -> log.info(user.toString()));});
	}

	public void convertToStringExample() {
		List<User> usersList = Arrays.asList(new User("Tom", "Cruise"),
				new User("Emilia", "Clark"),
				new User("Brad", "Pitt"),
				new User("Bruce", "Wills"),
				new User("Bruce", "Lee"));
		Flux.fromIterable(usersList)
				.map(user -> user.getName().toUpperCase().concat(" ").concat(user.getLastname().toUpperCase()))
				.flatMap(name -> {
					if (name.contains("bruce".toUpperCase())) {
						return Mono.just(name);
					} else {
						return Mono.empty();
					}
				})
				.map(name -> {
					return name.toLowerCase();
				}).subscribe(user -> log.info(user.toString()));
	}

	public void flatMapExample() {
		List<String> usersList = Arrays.asList("Tom Cruise", "Emilia Clark",
				"Brad Pitt", "Bruce Wills", "Bruce Lee");
		Flux.fromIterable(usersList)
				.map(name -> new User(name.split(" ")[0].toUpperCase(),
						name.split(" ")[1].toUpperCase()))
				.flatMap(user -> {
					if (user.getName().equalsIgnoreCase("bruce")) {
						return Mono.just(user);
					} else {
						return Mono.empty();
					}
				})
				.map(user -> {
					String name = user.getName().toLowerCase();
					user.setName(name);
					return user;
				}).subscribe(user -> log.info(user.toString()));
	}

	public void iterableExample() {
		List<String> usersList = Arrays.asList("Tom Cruise", "Emilia Clark",
				"Brad Pitt", "Bruce Wills", "Bruce Lee");
		Flux<String> names = Flux.fromIterable(usersList);
		// Flux.just("Tom Cruise", "Emilia Clark", "Brad Ptt", "Bruce Wills", "Bruce Lee");

		Flux<User> users = 	names.map(name -> new User(name.split(" ")[0].toUpperCase(),
						name.split(" ")[1].toUpperCase()))
				.filter(user -> user.getName().toLowerCase().equals("bruce"))
				.doOnNext(user -> {
					if (user == null) {
						throw new RuntimeException("A name is empty");
					}
					log.info(user.getName().concat(" ").concat(user.getLastname()));
				})
				.map(user -> {
					String name = user.getName().toLowerCase();
					user.setName(name);
					return user;
				});

		users.subscribe(user -> log.info(user.toString()),
				error -> log.error(error.getMessage()),
				new Runnable() {
					@Override
					public void run() {
						log.info("Task completed");
					}
				});
	}
}
