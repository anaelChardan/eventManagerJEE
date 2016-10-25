package fr.mines.event_manager.event.manager;

import fr.mines.event_manager.app.repository.TankRepository;
import fr.mines.event_manager.event.entity.Address;
import fr.mines.event_manager.event.entity.Event;
import fr.mines.event_manager.framework.manager.BaseEntityManager;
import fr.mines.event_manager.framework.repository.BaseEntityManagerWrapper;
import fr.mines.event_manager.framework.security.UserProvider;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Damien on 24/10/2016.
 */
public class EventManager implements BaseEntityManager<Event> {

    private static EventManager instance = null;

    private EventManager()
    {}

    public static EventManager getInstance()
    {
        if (null == instance)
        {
            instance = new EventManager();
        }

        return instance;
    }

    @Override
    public Event create(HttpServletRequest request) {
        // Gestion des dates
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        Date startDate = null;
        Date endDate = null;
        try {
            startDate = sdf.parse(request.getParameter("start_date"));
            endDate = sdf.parse(request.getParameter("end_date"));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return (new Event())
                .setAddress(AddressManager.getInstance().create(request))
                .setAuthor(UserProvider.getCurrentUser(request))
                .setDescription(request.getParameter("description"))
                .setMaxTickets(Integer.parseInt(request.getParameter("max_tickets")))
                .setName(request.getParameter("titre"))
                .setPrice(Double.parseDouble(request.getParameter("price")))
                .setStartDate(startDate)
                .setEndDate(endDate)
                .setPublished("create-and-publish".equals(request.getParameter("action")));
    }

    @Override
    public Event persist(Event object) {
        return TankRepository.getInstance().getEventRepository().create(object);
    }


}
