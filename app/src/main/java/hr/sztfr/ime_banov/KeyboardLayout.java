package hr.sztfr.ime_banov;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

@SuppressLint("ViewConstructor")
public class KeyboardLayout extends LinearLayout {

    Context context;
    LayoutInflater inflater;
    InputConnection inputConnection;
    int imeAction;

    private static final String CONSONANTS = "bcdfghjklmnpqrstvwxyz";
    private static final String VOWELS = "aeiou";
    int consonantIndex;
    int vowelIndex;
    boolean lowercase;

    ImageButton button_consonant_up;
    ImageButton button_consonant_down;
    ImageButton button_consonant_left;
    ImageButton button_consonant_right;
    ImageButton button_consonant_check;
    Button button_vowel_slide;
    ImageButton button_vowel_check;
    ImageButton button_delete;
    Button button_space;
    TextView textView_consonant;
    TextView textView_vowel;


    public KeyboardLayout(LayoutInflater inflater, Context ctx,
                          InputConnection inputConnection,
                          int currentIMEaction,
                          float kbHeight) {
        super(ctx);
        this.context = ctx;
        this.inflater = inflater;
        this.inputConnection = inputConnection;
        this.imeAction = currentIMEaction;

        // Postavljanje/aktualiziranje dimenzija tipkovnice (primarno visine pomocu
        // zadanog "tezinskog faktora" u odnosnu na cijelu visinu zaslona)
        DisplayMetrics dm = ctx.getResources().getDisplayMetrics();
        float kbScale = kbHeight * dm.heightPixels;
        int availableHeight = (int)kbScale;

        // Ucitavanje layout-a tipkovnice iz odgovarajuceg xml-a
        inflater.inflate(R.layout.keyboard_layout, this);
        LinearLayout rootView = this.findViewById(R.id.rootview);

        // Apliciranje visine tipkovnice promjenom parametra za vrsni UI-layout
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) rootView.getLayoutParams();
        params.height = availableHeight;
        rootView.setLayoutParams(params);

        // Instanciranje svih relevantnih view-ova
        instantiateViews();

        // Regitracija svih potrebnih listenera za buttone
        registerButtonListeners();

        // Postavljanje početnih vrijednosti samoglasnika i suglasika
        this.consonantIndex = 0;
        this.vowelIndex = 0;
        this.lowercase = false;
        button_vowel_slide.setText("A E I O U");
        setTextView(true);
        setTextView(false);
    }


    // Instanciranje svih relevantnih view-ova
    private void instantiateViews() {
        button_consonant_check = findViewById(R.id.button_consonant_check);
        button_consonant_up = findViewById(R.id.button_consonant_up);
        button_consonant_down = findViewById(R.id.button_consonant_down);
        button_consonant_left = findViewById(R.id.button_consonant_left);
        button_consonant_right = findViewById(R.id.button_consonant_right);
        button_vowel_check = findViewById(R.id.button_vowel_check);
        button_vowel_slide = findViewById(R.id.button_vowel_slide);

        button_delete = findViewById(R.id.button_delete);
        button_space = findViewById(R.id.button_space);

        textView_consonant = findViewById(R.id.textView_consonant);
        textView_vowel = findViewById(R.id.textView_vowel);
    }


    // Ažuriranje prikaza trenutno aktivnog samoglasnika ili suglasnika
    private void setTextView(boolean consonant) {
        char letter;
        if (consonant) {
            letter = lowercase ? CONSONANTS.charAt(consonantIndex) :
                    CONSONANTS.toUpperCase().charAt(consonantIndex);
            textView_consonant.setText(String.valueOf(letter));
        } else {
            letter = lowercase ? VOWELS.charAt(vowelIndex) :
                    VOWELS.toUpperCase().charAt(vowelIndex);
            textView_vowel.setText(String.valueOf(letter));
        }
    }


    // Registracija onClick, onTouch i onHover listenera za sve buttone
    // Omogucit ce se i klizanje po buttonu za samoglasniek (dodirom), te "lebdenje" iznad njega
    // Pri tome, ucinak akcije "lebdenja" se moze testirati putem mousea (OTG support)
    @SuppressLint("ClickableViewAccessibility")
    private void registerButtonListeners() {
        button_consonant_left.setOnClickListener((view) -> {
            if (consonantIndex == 0) {
                consonantIndex = CONSONANTS.length() -1;
            } else {
                consonantIndex--;
            }
            setTextView(true);
        });

        button_consonant_right.setOnClickListener((view) -> {
            if (consonantIndex == CONSONANTS.length() -1) {
                consonantIndex = 0;
            } else {
                consonantIndex++;
            }
            setTextView(true);
        });

        button_consonant_up.setOnClickListener((view) -> {
            button_vowel_slide.setText("A E I O U");
            lowercase = false;
            setTextView(true);
            setTextView(false);
        });

        button_consonant_down.setOnClickListener((view) -> {
            button_vowel_slide.setText("a e i o u");
            lowercase = true;
            setTextView(true);
            setTextView(false);
        });

        button_consonant_check.setOnClickListener((view) ->
            inputConnection.commitText(textView_consonant.getText(), 1)
        );

        button_vowel_check.setOnClickListener((view) ->
            inputConnection.commitText(textView_vowel.getText(), 1)
        );

        button_vowel_slide.setOnTouchListener(this::slideVowels);
        button_vowel_slide.setOnHoverListener(this::slideVowels);

        button_space.setOnClickListener((view) ->
            inputConnection.commitText(" ", 1)
        );

        button_delete.setOnClickListener((view) ->
            inputConnection.deleteSurroundingText(1, 0)
        );
    }


    // Ako postoji "klizanje" po buttonu za samoglasnike, ažuriraj prikaz samoglasnika
    private boolean slideVowels(View view, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            int insideIndex = checkInsideButton((int)event.getRawX(), (int)event.getRawY());
            if (insideIndex != -1) {
                vowelIndex = insideIndex;
                setTextView(false);
            }
        }
        return false;
    }


    // Provjera: nalazi li se tocka trenutnog dodira (na zaslonu) unutar buttona za samoglasnike?
    // Ulaz: kordinata dodira (pointerX, pointerY)
    // Izlaz: indeks buttona u glavnoj kolekciji (ako sadrzi tu tocku), -1 ako takav button ne postoji
    private int checkInsideButton(int pointerX, int pointerY) {
        int[] loc = new int[2];
        button_vowel_slide.getLocationOnScreen(loc);
        if (isInside(pointerX, pointerY,
                loc[0], loc[1],
                loc[0] + button_vowel_slide.getWidth(),
                loc[1] + button_vowel_slide.getHeight())) {
            float percent = (float) (pointerX - loc[0]) / button_vowel_slide.getWidth();
            return (int) (percent * 5);
        }
        return -1;
    }


    // Pomocna metoda za provjeru polozaja tocke dodira u geometriji buttona
    // Nalazi li se tocka (x, y) u pravokutniku (left, top, right, bottom)
    private boolean isInside(int pointerX, int pointerY,
                             int boundLeft, int boundTop, int boundRight, int boundBottom) {
        return ((pointerX >= boundLeft) && (pointerX <= boundRight) &&
                (pointerY >= boundTop) && (pointerY <= boundBottom));
    }

    public void setInputConnection(InputConnection ic) {
        this.inputConnection = ic;
    }

    public void setImeAction(int imeAction) {
        this.imeAction = imeAction;
    }
}