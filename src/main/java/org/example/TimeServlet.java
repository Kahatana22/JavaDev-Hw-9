package org.example;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.stream.Stream;

@WebServlet(value = "/time")
public class TimeServlet extends HttpServlet {
    private TemplateEngine engine;

    public void init() {
        engine = new TemplateEngine();
        ServletContext servletContext = this.getServletContext();
        String templatePath = servletContext.getRealPath("/WEB-INF/classes/templates/");

        FileTemplateResolver resolver = new FileTemplateResolver();
        resolver.setPrefix(templatePath);
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML5");
        resolver.setOrder(engine.getTemplateResolvers().size());
        resolver.setCacheable(false);
        engine.addTemplateResolver(resolver);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Cookie lastTimezone = Stream.ofNullable(req.getCookies())
                .flatMap(Arrays::stream)
                .filter(c -> c.getName().equals("lastTimezone"))
                .findFirst()
                .orElse(null);

        String timezone = req.getParameter("timezone");
        if (timezone == null && lastTimezone == null) {
            timezone = "UTC";
        } else if (timezone == null) {
            timezone = lastTimezone.getValue().replace(" ","+");
        } else {
            timezone = timezone.replace(" ","+");
            resp.addCookie(new Cookie("lastTimezone", timezone));
        }

        String currentTime = ZonedDateTime
                .now(ZoneId.of(timezone))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss ")) + timezone;

        Context context = new Context();
        context.setVariable("currentTime", currentTime);
        engine.process("index", context, resp.getWriter());
    }
}
