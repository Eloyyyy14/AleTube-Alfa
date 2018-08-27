package com.example.eloyyyyyyy.pruebasapiyoutube;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import static java.lang.Thread.sleep;

public class MainActivity extends YouTubeBaseActivity implements
        YouTubePlayer.OnInitializedListener, YouTubePlayer.PlaybackEventListener{

    Button siguienteVideo;
    TextView tvNombreVideo;
    TextView tvNombreCanal;
    TextView tvFechaSubida;
    View v;

    private String claveYT="AIzaSyD1ykwAYUodC9hA_kUrRRj7oCJXk8iPSYM";
    private YouTubePlayerView youTubePlayerView;
    private YouTubePlayer youTubePlayer1;
    //private String idVideo="DRS_PpOrUZ4"; //https://www.youtube.com/watch?v=azxDhcKYku4
    //private String urlInfoVideo="https://www.googleapis.com/youtube/v3/videos?part=id%2Csnippet&id="+idVideo+"&key="+claveYT;
    //private String urlStatsVideo="https://www.googleapis.com/youtube/v3/videos?part=statistics&id="+idVideo+"&key="+claveYT;
    ArrayList<Video> listVideo = new ArrayList<Video>();
    ArrayList<StatsVideo> listStatsVideo = new ArrayList<StatsVideo>();
    Video video=new Video();
    StatsVideo statsVideo = new StatsVideo();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        youTubePlayerView=(YouTubePlayerView)findViewById(R.id.youtube_view);
        youTubePlayerView.initialize(claveYT, this);
        siguienteVideo=(Button)findViewById(R.id.btnSiguienteVideo);
        tvNombreVideo=(TextView)findViewById(R.id.tvNombreVideo);
        tvNombreCanal=(TextView)findViewById(R.id.tvNombreCanal);
        tvFechaSubida=(TextView)findViewById(R.id.tvFecha);

        siguienteVideo(v);

    }
//-----------------------------------------------------------------------------------------------------------------------------
//Mis Metodos

    //Saco 5 caracteres aleatorios para hacer una búsqueda de vídeos que contengan un id de video con esos caracteres
    public String obtenerRandomIdVideo(){
        String[] listLetras={"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p",
                "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "_", "-"};

        String a="";
        String b="";
        String c="";
        String d="";
        String e="";

        int rnd = new Random().nextInt(listLetras.length);
        a=listLetras[rnd];

        int rnd1 = new Random().nextInt(listLetras.length);
        b=listLetras[rnd1];

        int rnd2 = new Random().nextInt(listLetras.length);
        c=listLetras[rnd2];

        int rnd3 = new Random().nextInt(listLetras.length);
        d=listLetras[rnd3];

        int rnd4 = new Random().nextInt(listLetras.length);
        e=listLetras[rnd4];

        String id= a + b +  c +  d + e;

        System.out.println("String a buscar(obtenerRandomIdVideo): "+id);

        return id;
    }

    //Sacar datos de video del json
    public void sacarJsonInfoVideo(String url){

        RequestQueue request = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    JSONObject jsonObject = new JSONObject(response.toString(0));

                    JSONArray jsonArray=jsonObject.getJSONArray("items");

                    for(int i=0; i<jsonArray.length(); i++){
                        String idVideo=jsonArray.getJSONObject(i).getJSONObject("id").getString("videoId");
                        video.setIdVideo(idVideo);

                        String tituloVideo=jsonArray.getJSONObject(i).getJSONObject("snippet").getString("title");
                        video.setTitulo(tituloVideo);

                        String fechaSubida=jsonArray.getJSONObject(i).getJSONObject("snippet").getString("publishedAt");
                        String SfechaSubida=fechaSubida.substring(0, 10);
                        video.setDiaSubida(SfechaSubida);

                        String nombreCanal=jsonArray.getJSONObject(i).getJSONObject("snippet").getString("channelTitle");
                        video.setNombreCanal(nombreCanal);

                        String miniatura=jsonArray.getJSONObject(i).getJSONObject("snippet").getJSONObject("thumbnails").getJSONObject("default").getString("url");
                        video.setMiniatura(miniatura);

                        listVideo.add(video);
                    }

                    String idVideos = listVideo.get(0).getIdVideo();
                    System.out.println("Id a reproducir YA(sacarJsonInfoVideo): "+ idVideos);

                    //youTubePlayer1.loadVideo(idVideos);
                    //youTubePlayer1.play();

                    tvNombreVideo.setText(listVideo.get(0).getTitulo());
                    tvNombreCanal.setText(listVideo.get(0).getNombreCanal());
                    tvFechaSubida.setText(listVideo.get(0).getDiaSubida());

                    String urlStatsVideo="https://www.googleapis.com/youtube/v3/videos?part=statistics&id="+idVideos+"&key="+claveYT;

                    sacarJsonStats(urlStatsVideo, idVideos);


                } catch (JSONException e) {
                    siguienteVideo(v);
                    Toast.makeText(getApplicationContext(), "Holi", Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        request.add(jsonObjectRequest);
    }

    //Sacar datos de estadisticas json a partir de una url y una idVideo para reproducir
    public void sacarJsonStats(String url, final String idVideo){
        RequestQueue request = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                int visitas=0;
                    try {
                        JSONObject jsonObject = new JSONObject(response.toString(0));

                        JSONArray jsonArray = jsonObject.getJSONArray("items");

                        for (int i = 0; i < jsonArray.length(); i++) {
                            String numVisitas = jsonArray.getJSONObject(i).getJSONObject("statistics").getString("viewCount");
                            statsVideo.setVisitas(numVisitas);

                            listStatsVideo.add(statsVideo);
                        }

                        String visitasS = statsVideo.getVisitas();
                        visitas=Integer.parseInt(visitasS);
                        System.out.println("Numero de visitas video dentro bucle(sacarJsonStats): " + visitas);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                if(visitas<1000) {
                    youTubePlayer1.loadVideo(idVideo);
                    youTubePlayer1.play();
                    System.out.println("Numero de visitas video salida bucle(sacarJsonStats): " + visitas);
                }
                else{
                        siguienteVideo(v);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        request.add(jsonObjectRequest);
    }

    //Metodo al pulsar el botón
    public void siguienteVideo(View v){

        String idVideo=obtenerRandomIdVideo();
        System.out.println("Id URL(siguienteVideo): "+idVideo);

        String urlBuscarVideo="https://www.googleapis.com/youtube/v3/search?part=id,snippet&maxResults=5&type=video&q="+idVideo+"&key="+claveYT;

        sacarJsonInfoVideo(urlBuscarVideo);
    }

//Fin Mis Metodos
//------------------------------------------------------------------------------------------------------------

    //Método para comprobar si fue bien
    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {

        //Si fue bien entonces carga y reproduce el video
        if(!b){
            youTubePlayer1=youTubePlayer;
            youTubePlayer1.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);

            //Carga y reproduce directamente el video
            youTubePlayer1.loadVideo("DRS_PpOrUZ4");
            youTubePlayer1.play();
        }
    }

    //Método para comprobar si algo fue mal
    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

        //Si existió algún error al inicializar muestra un dialog con el error
        if(youTubeInitializationResult.isUserRecoverableError()){
            youTubeInitializationResult.getErrorDialog(this, 1).show();
        }
        //Si YT no sabe cual es el error enviará este mensaje
        else{
            Toast.makeText(getApplicationContext(), "Error al inializar YouTube "+youTubeInitializationResult.toString(), Toast.LENGTH_LONG).show();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){
        //Si se ha resuelto el error ¿?
        if(resultCode==1){
            getYouTubePlayerView().initialize(claveYT, this);
        }
    }

    public YouTubePlayerView getYouTubePlayerView() {
        return youTubePlayerView;
    }

    @Override
    public void onPlaying() {

    }

    @Override
    public void onPaused() {

    }

    @Override
    public void onStopped() {

    }

    @Override
    public void onBuffering(boolean b) {

    }

    @Override
    public void onSeekTo(int i) {

    }
}
