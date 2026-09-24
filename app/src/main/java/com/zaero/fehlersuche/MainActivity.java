package com.zaero.fehlersuche;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import java.io.*;
import java.util.*;
import org.json.*;

public class MainActivity extends Activity {

    static final String PREFS = "zaero_data";
    static final String KEY_ERRORS = "errors";
    static final String MASTER = "Chefe";

    final int GOLD = Color.rgb(214,168,79);
    final int BG = Color.rgb(18,15,15);
    final int PANEL = Color.rgb(31,26,26);
    final int TEXT = Color.rgb(245,240,232);
    final int MUTED = Color.rgb(190,180,170);

    LinearLayout root, content;
    TextView roleView;
    boolean profi = false;

    ArrayList<ErrorItem> errors = new ArrayList<>();

    int dp(float v) {
        return (int)(v * getResources().getDisplayMetrics().density + 0.5f);
    }

    TextView tv(String s, float sp) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(TEXT);
        t.setPadding(dp(12), dp(8), dp(12), dp(8));
        return t;
    }

    Button btn(String s) {
        Button b = new Button(this);
        b.setText(s);
        b.setTextColor(TEXT);
        b.setTextSize(15);
        b.setAllCaps(false);
        b.setBackground(round(PANEL, dp(10)));
        return b;
    }

    GradientDrawable round(int c, int r) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(c);
        g.setCornerRadius(r);
        g.setStroke(dp(1), Color.rgb(85,72,58));
        return g;
    }

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(BG);
        load();
        build();
    }

    // --------------------------------------------------
    // HAUPTSEITE
    // --------------------------------------------------

    void build() {

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);

        // Kopfzeile
        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(dp(8), dp(6), dp(8), dp(6));

        Button menu = btn("☰");
        menu.setTextSize(25);

        top.addView(
                menu,
                new LinearLayout.LayoutParams(dp(55), dp(52))
        );

        TextView logo = tv("Z.AERO", 20);
        logo.setTextColor(GOLD);
        logo.setGravity(Gravity.CENTER);

        top.addView(
                logo,
                new LinearLayout.LayoutParams(0, dp(52), 1)
        );

        roleView = tv(profi ? "PROFI" : "BENUTZER", 13);
        roleView.setTextColor(GOLD);
        roleView.setGravity(Gravity.CENTER);

        top.addView(
                roleView,
                new LinearLayout.LayoutParams(dp(105), dp(52))
        );

        root.addView(top);

        // Inhalt
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(
                dp(14),
                dp(8),
                dp(14),
                dp(18)
        );

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.addView(content);

        root.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );

        setContentView(root);

        menu.setOnClickListener(v -> showMenu());

        roleView.setOnClickListener(v -> loginDialog());

        renderHome();
    }

    TextView welcomeText(String text, float size, int color) {

        TextView t = tv(text, size);

        t.setTextColor(color);
        t.setGravity(Gravity.CENTER);
        t.setTypeface(
                android.graphics.Typeface.DEFAULT,
                android.graphics.Typeface.BOLD
        );

        return t;
    }

    void animateIn(View view, long delay) {

        view.setTranslationX(-dp(90));
        view.setAlpha(0f);

        view.animate()
                .translationX(0)
                .alpha(1f)
                .setStartDelay(delay)
                .setDuration(700)
                .setInterpolator(
                        new android.view.animation.DecelerateInterpolator()
                )
                .start();
    }

    void renderHome() {

        content.removeAllViews();

        TextView welcome =
                welcomeText(
                        "Willkommen bei",
                        25,
                        TEXT
                );

        content.addView(
                welcome,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(48)
                )
        );

        animateIn(welcome, 0);


        TextView brand =
                welcomeText(
                        "Z-Aero Diamond Clean",
                        31,
                        GOLD
                );

        content.addView(
                brand,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(58)
                )
        );

        animateIn(brand, 160);


        TextView subtitle =
                tv(
                        "Ihre Unterstützung für den sicheren\n" +
                        "und effizienten Betrieb der Anlage.",
                        16
                );

        subtitle.setGravity(Gravity.CENTER);

        content.addView(
                subtitle,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(66)
                )
        );

        animateIn(subtitle, 320);


        // Maschinenbereich
        LinearLayout machine =
                new LinearLayout(this);

        machine.setOrientation(
                LinearLayout.VERTICAL
        );

        machine.setGravity(Gravity.CENTER);

        machine.setPadding(
                dp(10),
                dp(10),
                dp(10),
                dp(10)
        );

        machine.setBackground(
                round(
                        Color.rgb(25,22,22),
                        dp(18)
                )
        );

        TextView machineIcon =
                welcomeText(
                        "⚙",
                        72,
                        GOLD
                );

        machine.addView(
                machineIcon,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(105)
                )
        );

        TextView machineName =
                welcomeText(
                        "Z.AERO",
                        27,
                        GOLD
                );

        machine.addView(
                machineName,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(48)
                )
        );

        TextView machineText =
                tv(
                        "ANLAGE",
                        15
                );

        machineText.setGravity(Gravity.CENTER);

        machine.addView(
                machineText,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(40)
                )
        );

        LinearLayout.LayoutParams machineParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(225)
                );

        machineParams.setMargins(
                dp(4),
                dp(4),
                dp(4),
                dp(12)
        );

        content.addView(
                machine,
                machineParams
        );

        animateIn(machine, 420);


        // Drei Hauptkarten
        LinearLayout cards =
                new LinearLayout(this);

        cards.setOrientation(
                LinearLayout.HORIZONTAL
        );

        cards.setGravity(Gravity.CENTER);

        content.addView(
                cards,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(205)
                )
        );


        addHomeCard(
                cards,
                "▶",
                "Anfahren",
                "Anlage starten",
                () -> renderChapter("Anfahren")
        );

        addHomeCard(
                cards,
                "■",
                "Abstellen",
                "Anlage sicher\nherunterfahren",
                () -> renderChapter("Abstellen")
        );

        addHomeCard(
                cards,
                "⌕",
                "Fehlersuche",
                "Fehler und\nLösungen",
                () -> showTroubleshooting()
        );

        animateIn(cards, 560);
    }

    void addHomeCard(
            LinearLayout parent,
            String icon,
            String title,
            String subtitle,
            final Runnable action) {

        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.VERTICAL
        );

        card.setGravity(Gravity.CENTER);

        card.setPadding(
                dp(6),
                dp(8),
                dp(6),
                dp(8)
        );

        card.setBackground(
                round(PANEL, dp(16))
        );


        TextView ic =
                welcomeText(
                        icon,
                        30,
                        GOLD
                );

        card.addView(
                ic,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(58)
                )
        );


        TextView titleView =
                welcomeText(
                        title,
                        18,
                        TEXT
                );

        card.addView(
                titleView,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(38)
                )
        );


        TextView sub =
                tv(
                        subtitle,
                        13
                );

        sub.setGravity(Gravity.CENTER);
        sub.setTextColor(MUTED);

        card.addView(
                sub,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(50)
                )
        );


        card.setOnClickListener(
                v -> action.run()
        );


        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        0,
                        -1,
                        1
                );

        p.setMargins(
                dp(4),
                0,
                dp(4),
                0
        );

        parent.addView(card, p);
    }

    // --------------------------------------------------
    // ANFAHREN / ABSTELLEN
    // --------------------------------------------------

    void renderChapter(String chapter) {

        content.removeAllViews();

        Button back =
                btn("‹  Startseite");

        back.setTextColor(GOLD);

        content.addView(
                back,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(50)
                )
        );

        back.setOnClickListener(
                v -> renderHome()
        );


        TextView heading =
                welcomeText(
                        chapter,
                        28,
                        GOLD
                );

        content.addView(
                heading,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(70)
                )
        );


        String text;

        if (chapter.equals("Anfahren")) {

            text =
                    "Hier wird später Schritt für Schritt " +
                    "beschrieben, wie die Anlage gestartet wird.";

        } else {

            text =
                    "Hier wird später Schritt für Schritt " +
                    "beschrieben, wie die Anlage sicher abgestellt wird.";
        }


        TextView body =
                tv(text, 17);

        body.setGravity(Gravity.CENTER);

        content.addView(
                body,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(180)
                )
        );

        animateIn(heading, 0);
        animateIn(body, 180);
    }

    // --------------------------------------------------
    // FEHLERSUCHE
    // --------------------------------------------------

    void showTroubleshooting() {

        content.removeAllViews();


        Button back =
                btn("‹  Startseite");

        back.setTextColor(GOLD);

        content.addView(
                back,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(50)
                )
        );

        back.setOnClickListener(
                v -> renderHome()
        );


        TextView heading =
                welcomeText(
                        "Fehlersuche",
                        28,
                        GOLD
                );

        content.addView(
                heading,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(60)
                )
        );


        Button search =
                btn("⌕  Fehler suchen");

        search.setTextSize(18);

        content.addView(
                search,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(58)
                )
        );

        search.setOnClickListener(
                v -> showSearch()
        );


        if (profi) {

            Button add =
                    btn("＋ Fehler anlegen");

            content.addView(
                    add,
                    new LinearLayout.LayoutParams(
                            -1,
                            dp(58)
                    )
            );

            add.setOnClickListener(
                    v -> editError(null)
            );
        }


        renderAllErrors();

        animateIn(heading, 0);
    }

    void renderAllErrors() {

        if (errors.isEmpty()) {

            TextView empty =
                    tv(
                            "Noch keine Fehler gespeichert.\n" +
                            "Als Profi kannst du hier Fehler " +
                            "und Lösungen anlegen.",
                            16
                    );

            empty.setGravity(Gravity.CENTER);

            content.addView(
                    empty,
                    new LinearLayout.LayoutParams(
                            -1,
                            dp(160)
                    )
            );

            return;
        }


        for (ErrorItem e : errors) {

            Button b =
                    btn(
                            "FEHLER  " +
                            e.title +
                            "\n" +
                            e.description
                    );

            b.setGravity(
                    Gravity.START |
                    Gravity.CENTER_VERTICAL
            );

            content.addView(
                    b,
                    new LinearLayout.LayoutParams(
                            -1,
                            dp(92)
                    )
            );

            b.setOnClickListener(
                    v -> showError(e)
            );
        }
    }

    // --------------------------------------------------
    // MENÜ
    // --------------------------------------------------

    void showMenu() {

        final Dialog d =
                new Dialog(this);

        LinearLayout l =
                dialogBox();

        TextView heading =
                tv("Menü", 20);

        heading.setTextColor(GOLD);

        l.addView(heading);


        Button home =
                btn("Startseite");

        Button start =
                btn("Anfahren");

        Button stop =
                btn("Abstellen");

        Button errorsButton =
                btn("Fehlersuche");


       
