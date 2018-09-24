package app.devlife.connect2sql.di;

import javax.inject.Singleton;

import app.devlife.connect2sql.db.provider.AppContentProvider;
import dagger.Component;

/**
 *
 */
@Singleton
@Component(modules = {DatabaseModule.class, SecurityModule.class})
public interface ContentProviderComponent {
    void inject(AppContentProvider appContentProvider);
}
