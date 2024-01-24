package uni.tesis.interfazfinal;

import android.content.Context;
import android.content.SharedPreferences;

public class TiempoUsoApp {
    private long tiempoInicio;
    private long tiempoTotal;

    private static final String PREF_NAME = "TiempoUsoPrefs";

    private static final String KEY_TIEMPO_INICIO = "tiempoInicio";

    private SharedPreferences sharedPreferences;

    public TiempoUsoApp(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void iniciarMedicion() {
        sharedPreferences.edit().putLong(KEY_TIEMPO_INICIO, System.currentTimeMillis()).apply();
    }

    public long detenerMedicion() {
        long tiempoInicio = sharedPreferences.getLong(KEY_TIEMPO_INICIO, 0);
        return System.currentTimeMillis() - tiempoInicio;

    }
    

}
