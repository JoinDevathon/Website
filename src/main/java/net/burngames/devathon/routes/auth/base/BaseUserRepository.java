package net.burngames.devathon.routes.auth.base;

import com.google.common.util.concurrent.*;
import net.burngames.devathon.persistence.users.UserDatabase;
import net.burngames.devathon.routes.auth.AccountInfo;
import net.burngames.devathon.routes.auth.UserRepository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class BaseUserRepository implements UserRepository {

    private final UserDatabase db;

    private final Map<String, AccountInfo> loadedUsers;

    private final ListeningExecutorService threadPool;

    private static final int NUM_THREADS_IN_POOL = 4;

    public BaseUserRepository(UserDatabase db) {
        this.db = db;

        this.loadedUsers = new ConcurrentHashMap<>();

        threadPool = MoreExecutors.listeningDecorator(
                Executors.newFixedThreadPool(
                        NUM_THREADS_IN_POOL,
                        new ThreadFactoryBuilder().setNameFormat("User Repository Pool #%d").build()
                )
        );
    }

    @Override
    public AccountInfo getUser(String username) {
        return loadedUsers.get(username);
    }

    @Override
    public boolean isUserLoaded(String username) {
        return loadedUsers.containsKey(username);
    }

    @Override
    public void loadUserAndRun(String username, Consumer<AccountInfo> onFinish) {
        AccountInfo info = getUser(username);

        if (info != null) {
            onFinish.accept(info);

            return;
        }

        ListenableFuture<AccountInfo> ret = threadPool.submit(() -> db.getUserByUsername(username));

        Futures.addCallback(ret, new FutureCallback<AccountInfo>() {
            @Override
            public void onSuccess(AccountInfo i) {
                if (i == null) {
                    // no account, fuck this
                    return;
                }

                loadedUsers.put(username, i);

                onFinish.accept(i);
            }

            @Override
            public void onFailure(Throwable thrown) {
                // oh well
            }
        });
    }


}
