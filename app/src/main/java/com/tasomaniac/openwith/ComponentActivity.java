package com.tasomaniac.openwith;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

public abstract class ComponentActivity<Component> extends AppCompatActivity {

    private Component component;

    protected final Component getComponent() {
        if (component == null) {
            component = getLastCustomNonConfigurationInstance();
        }
        if (component == null) {
            component = createComponent();
        }
        return component;
    }

    protected abstract Component createComponent();

    @Override
    public final Component onRetainCustomNonConfigurationInstance() {
        return component;
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public final Component getLastCustomNonConfigurationInstance() {
        return (Component) super.getLastCustomNonConfigurationInstance();
    }
}
