package me.jromero.connect2sql.di;

import javax.inject.Singleton;

import dagger.Component;
import me.jromero.connect2sql.db.provider.AppContentProvider;

/**
 *
 */
@Singleton
@Component(modules = {DatabaseModule.class, SecurityModule.class})
public interface ContentProviderComponent {

    void inject(AppContentProvider appContentProvider);
}
