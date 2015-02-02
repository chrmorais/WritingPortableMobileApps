package br.com.epx.photor;

public abstract class Model {
    protected  photorActivity observer;

    public Model(photorActivity observer) {
        this.observer = observer;
    }

    public abstract void reset();
    public abstract void model_input(String name, int pos);
    public abstract void model_sample_picture(String iso, String aperture, String shutter);
    public abstract void model_list(String name);
}
