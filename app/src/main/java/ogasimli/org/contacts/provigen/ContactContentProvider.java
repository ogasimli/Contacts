package ogasimli.org.contacts.provigen;

import com.tjeannin.provigen.ProviGenOpenHelper;
import com.tjeannin.provigen.ProviGenProvider;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;

import ogasimli.org.contacts.helper.Constants;

/**
 * Created by Orkhan Gasimli on 10.05.2017.
 */

public class ContactContentProvider extends ProviGenProvider {

    private static final Class[] contracts = new Class[]{ContactContract.class};

    @Override
    public SQLiteOpenHelper openHelper(Context context) {
        return new ProviGenOpenHelper(getContext(), Constants.DB_NAME, null, Constants.DB_VERSION,
                contracts);
    }

    @Override
    public Class[] contractClasses() {
        return contracts;
    }
}
