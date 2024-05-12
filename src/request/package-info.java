/**
 * package request contient les classes qui aide a la representation
 * du langage de requete
 *
 * sauf les classes ASTS qui representent chaque type de requete
 * il y a aussi une class Request qui instancie une requete
 * et une class RequestContinuation qui instancie une requete de continuation
 *
 * L'interprete est un "visiteur" qui visite chaque noeud de l'arbre pour
 * evaluer la requete
 *
 * 2 classes de tests:
 * Test qui est la premiere version de test pour tester les requetes
 * JUnitTest qui implemente les tests unitaires (17) pour tester les requetes
 * de maniere complete
 */
package request;
