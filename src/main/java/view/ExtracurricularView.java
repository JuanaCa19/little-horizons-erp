package view;

import java.util.Objects;
import static view.ViewSupport.menu;

public class ExtracurricularView {
    private final ClubView clubs;
    private final EventView events;
    public ExtracurricularView() { this(new ClubView(),new EventView()); }
    public ExtracurricularView(ClubView clubs,EventView events) { this.clubs=Objects.requireNonNull(clubs);this.events=Objects.requireNonNull(events); }
    public void start() { menu("Actividades Extracurriculares","Volver al menu principal",new String[]{"Clubes y membresias","Eventos y participantes"},clubs::start,events::start); }
}
