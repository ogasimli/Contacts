package ogasimli.org.contacts.api;

import ogasimli.org.contacts.object.ContactList;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

/**
 * Created by Orkhan Gasimli on 10.05.2017.
 */

public interface ContactApiService {

    @GET("contacts/")
    Call<ContactList> getContactList();

    public static final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://api.androidhive.info/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();
}
