/**
 * packge nodes contiens les class qui aide a la representation des noeuds
 * dans le reseau
 *
 * plugins: une class nodePlugin qui delegue les operations de traiter les
 * requetes,d'envoyer les requetes..
 * Nous gardons les fonctions de changer ses propres valeurs des capteurs dans
 * la class Node
 *
 * connectors:nodeNodeConnector qui permet de connecter 2 noeuds
 * NodeRegistreConnector qui permet de connecter un noeud a un registre
 * NodeSensorConnector qui permet de connecter un noeud et un client
 *
 * ports:NodeInboundPort qui permet de recevoir les requetes
 * NodeOutboundPort qui permet d'envoyer les requetes
 * SensorNodeAsynReqyesrOutboundPort qui permet d'envoyer les resultats au client
 *
 * une class nodeinfo qui instancie les informations d'un noeud
 * une class SensorNodeComponent qui instancie un noeud
 */
package nodes;
