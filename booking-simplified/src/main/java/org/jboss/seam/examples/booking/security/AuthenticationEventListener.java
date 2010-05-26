package org.jboss.seam.examples.booking.security;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.context.spi.Context;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.AnnotationLiteral;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.jboss.seam.examples.booking.account.Registered;
import org.jboss.seam.examples.booking.model.User;
import org.jboss.seam.security.events.LoggedInEvent;
import org.jboss.seam.security.events.LoggedOutEvent;
import org.jboss.seam.web.HttpSessionManager;
import org.slf4j.Logger;

/**
 * An observer that monitors authentication events and performs
 * initialization and cleanup.
 * 
 * @author Dan Allen
 */
@RequestScoped
public class AuthenticationEventListener
{
   @Inject private Logger log;
   
   /**
    * Clear the dummy register user when a login event occurs. Temporary workaround
    * for not being able to clear this out some other way.
    */
   public void onLogin(@Observes LoggedInEvent loggedInEvent, BeanManager manager)
   {
      log.info(loggedInEvent.getPrincipal().getName() + " has logged in; clearing instance of @Registered User");
      Bean<User> registeredUserBean = (Bean<User>) manager.getBeans(User.class, new AnnotationLiteral<Registered>() {}).iterator().next();
      Context sessionContext = manager.getContext(SessionScoped.class);
      
      // TODO - the BeanStore.remove() method is no longer available - find a workaround
      //((AbstractThreadLocalMapContext) sessionContext).getBeanStore().remove(registeredUserBean);
   }
   
   /**
    * Observe the logout event and prepare the session to be terminated. We
    * cannot terminate the session immediately or else it will cause any
    * additional session-scoped observers to fail. It's necessary to delegate
    * this task to the Seam HttpSessionManager, which can terminate (i.e.,
    * invalidate) the session when the current request ends.
    */
   public void onLogout(@Observes LoggedOutEvent loggedOutEvent, HttpSessionManager sessionManager)
   {
      log.info(loggedOutEvent.getPrincipal().getName() + " has logged out");
      sessionManager.invalidateAtEndOfRequest();
   }
   
}
