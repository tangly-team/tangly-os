package net.tangly.app.api;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.html.H1;


@PageTitle("tangly Vaadin UI")
@Route("")
public class HelloWorldUI extends VerticalLayout {
    public HelloWorldUI() {
        add(new H1("Hello World"));
    }

}
