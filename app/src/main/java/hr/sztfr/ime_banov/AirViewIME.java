package hr.sztfr.ime_banov;

import android.content.Context;
import android.content.res.Configuration;
import android.inputmethodservice.InputMethodService;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;

public class AirViewIME extends InputMethodService {

    Context context;
    LayoutInflater inflater;
    KeyboardLayout layout;
    float KEYBOARD_HEIGHT = 0.285f;


    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        inflater = this.getLayoutInflater();

        // Sprjecavanje zatamnjivanja ekrana (dimming) nakon odredjenog perioda neaktivnosti:
        if (getWindow().getWindow() != null) {
            getWindow().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }


    // Called when the input method window has been shown to the user,
    // after previously not being visible
    @Override
    public void onWindowShown() {
        enableKeyboard();
    }


    // Instanciranje layout-a tipkovnice sa svim potrebnim postavkama
    // i konkretno apliciranje tih postavki
    // Sva aplikacijska logika tipkovnice smjestena je u razred koji opisuje/definira
    // ponasanje te tipkovnice (KeyboardLayout.java)
    private void enableKeyboard() {
        // Instranciranje layouta tipkovnice
        layout = new KeyboardLayout(inflater,
                context,
                this.getCurrentInputConnection(),
                this.getImeAction(this.getCurrentInputEditorInfo().imeOptions),
                KEYBOARD_HEIGHT);

        // Apliciranje custom tipkovnice
        setInputView(layout);

        //Re-evaluate whether the soft input area should currently be shown
        updateInputViewShown();
    }


    // Dohvat IME akcije za doticni editor na kojeg se unosna metoda trenutno odnosi
    private int getImeAction(int imeAction) {
        int action;
        switch (imeAction & (EditorInfo.IME_MASK_ACTION | EditorInfo.IME_FLAG_NO_ENTER_ACTION)) {
            case EditorInfo.IME_ACTION_DONE:
                action = EditorInfo.IME_ACTION_DONE;
                break;
            case EditorInfo.IME_ACTION_GO:
                action = EditorInfo.IME_ACTION_GO;
                break;
            case EditorInfo.IME_ACTION_NEXT:
                action = EditorInfo.IME_ACTION_NEXT;
                break;
            case EditorInfo.IME_ACTION_SEARCH:
                action = EditorInfo.IME_ACTION_SEARCH;
                break;
            case EditorInfo.IME_ACTION_SEND:
                action = EditorInfo.IME_ACTION_SEND;
                break;
            default:
                action = EditorInfo.IME_ACTION_UNSPECIFIED;
                break;
        }
        return action;
    }


    // Azuriranje input connection objekta, tako da se input stream
    // moze aplicirati prilikom (re)starta bilo kojeg editora u bilo kojoj aplikaciji.
    @Override
    public void onStartInput (EditorInfo attribute, boolean restarting) {
        if (layout != null) {
            layout.setInputConnection(this.getCurrentInputConnection());
            layout.setImeAction(this.getImeAction(attribute.imeOptions));
        }
    }


    // Promjena konfiguracije (tipicno: promjena orijentacije uredjaja)
    // "re-inicijalizacija" tipkovnice:
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        enableKeyboard();
    }


    // Oslobadjanje resursa, npr. ako se koriste neki listeneri, timeri i slicno...
    @Override
    public void onFinishInputView(boolean finishingInput) { }

}
