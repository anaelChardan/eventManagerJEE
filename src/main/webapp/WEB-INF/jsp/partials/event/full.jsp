<%@ taglib prefix="add" uri="application" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; UTF-8" language="java" pageEncoding="UTF-8" %>

<div class="container-fluid" style="background-color:#e9e9e9">
    <div class="row col-md-8 col-md-offset-2" style="border-radius:6px;background-color:#c4c4c4;margin-bottom:30px">

        <div class="row col-md-6 col-md-offset-3 col-sm-6 col-sm-offset-3" style="margin-bottom:50px">
            <h3 class="text-center" >${event.name}</h3>
        </div>

        <div class="row col-md-10">
            <dl class="dl-horizontal col-md-9">
                <dt>Description : </dt>
                <dd>${event.description}</dd>
                <dt>Date de début : </dt>
                <dd>${event.startDate}</dd>
                <dt>Date de fin : </dt>
                <dd>${event.endDate}</dd>
                <dt>Lieu : </dt>
                <dd>${event.address.address1}
                    ${event.address.address2}
                    ${event.address.zipCode} ${event.address.city}
                    ${event.address.country}
                </dd>
                <dt>Prix : </dt>
                <dd>${event.price}</dd>
                <dt>Nombre de places : </dt>
                <dd>${event.maxTickets}</dd>
            </dl>
        </div>
        <div class="row col-md-12">
            <table class="table">
                <caption>
                    <h4 class="text-left">Liste des participants</h4>
                </caption>
                <thead>
                <tr>
                    <th class="col-md-2 text-left">Nom</th>
                    <th class="col-md-2 text-left">Prenom</th>
                    <th class="col-md-6 text-left">Mail</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${event.subscribers}" var="user" >
                    <tr>
                        <td>${user.firstName}</td>
                        <td>${user.lastName}</td>
                        <td>${user.email}</td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>

            <form class="col-md-12" method="post" action="<app:PathTag endpoint="/event/action"/>">
                <div class="form-group form-inline">
                    <c:if test="${event.author == userConnected}">
                        <button type="submit" value="cancel" name="action" class="pull-right btn btn-danger" style="margin-right:20px">Annuler l'évènement</button>
                    </c:if>
                    <c:if test="${(not event.published) && (event.author == userConnected)}">
                        <button type="submit" value="publish" name="action" class="pull-right btn btn-info" style="margin-right:20px">Publier</button>
                        <button type="submit" value="modify" name="action" class="pull-right btn btn-info" style="margin-right:20px">Modifier</button>
                    </c:if>
                    <c:if test="${event.published}">
                        <button type="submit" value="subscribe" class="pull-right btn btn-info" style="margin-right:20px">S'inscrire</button>
                    </c:if>
                </div>
            </form>

        </div>

    </div>
</div>
</div>
