package com.tasomaniac.openwith.resolver;

interface ResolverPresenter {

    void bind(ResolverView view, ResolverView.Navigation navigation);

    void unbind(ResolverView view);
}
