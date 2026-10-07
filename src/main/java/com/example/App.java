package com.example;

import com.example.model.Produit;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.math.BigDecimal;
import java.util.List;
import org.h2.tools.Server;

public class App {
    public static void main(String[] args) {
        // demarrer la console H2 pour voir les tables dans le navigateur
        try {
            Server.createWebServer("-web", "-webPort", "8082").start();
            System.out.println("Console H2 disponible sur : http://localhost:8082");
        } catch (Exception e) {
            System.out.println("Erreur lors du démarrage de la console H2");
            e.printStackTrace();
        }

        // Création de l'EntityManagerFactory
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hibernate-demo");

        // Insertion de produits
        insererProduits(emf);

        // Lecture des produits
        lireProduits(emf);

        // --- Exemples supplementaires pour bien comprendre ---

        // ex: modifier le prix d'un produit
        modifierProduit(emf);

        // ex: supprimer un produit
        supprimerProduit(emf);

        // on relit pour voir les changements
        lireProduits(emf);

        // on garde l'appli ouverte pour consulter la console H2
        System.out.println("\nAppuyez sur Entrée pour quitter...");
        try {
            System.in.read();
        } catch (Exception e) {
            // rien
        }

        // Fermeture de l'EntityManagerFactory
        emf.close();
    }

    private static void insererProduits(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            // Création de quelques produits
            Produit p1 = new Produit("Laptop", new BigDecimal("999.99"));
            Produit p2 = new Produit("Smartphone", new BigDecimal("499.99"));
            Produit p3 = new Produit("Tablette", new BigDecimal("299.99"));

            // Persistance des produits
            em.persist(p1);
            em.persist(p2);
            em.persist(p3);

            // ex: j'ajoute un 4eme produit pour tester
            Produit p4 = new Produit("Casque audio", new BigDecimal("79.90"));
            em.persist(p4);

            em.getTransaction().commit();
            System.out.println("Produits insérés avec succès !");
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    private static void lireProduits(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        try {
            // Requête JPQL pour récupérer tous les produits
            List<Produit> produits = em.createQuery("SELECT p FROM Produit p", Produit.class)
                                      .getResultList();

            System.out.println("\nListe des produits :");
            for (Produit produit : produits) {
                System.out.println(produit);
            }

            // Recherche d'un produit par ID
            System.out.println("\nRecherche du produit avec ID=2 :");
            Produit produit = em.find(Produit.class, 2L);
            if (produit != null) {
                System.out.println(produit);
            } else {
                System.out.println("Produit non trouvé");
            }

            // ex: chercher les produits dont le prix est > 300
            System.out.println("\nProduits avec prix > 300 :");
            List<Produit> chers = em.createQuery(
                    "SELECT p FROM Produit p WHERE p.prix > :prixMin", Produit.class)
                    .setParameter("prixMin", new BigDecimal("300"))
                    .getResultList();
            for (Produit p : chers) {
                System.out.println("  -> " + p);
            }
        } finally {
            em.close();
        }
    }

    // ex: methode pour modifier un produit existant
    private static void modifierProduit(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            // on recupère le produit id=1 (le Laptop)
            Produit laptop = em.find(Produit.class, 1L);
            if (laptop != null) {
                System.out.println("\nAvant modif: " + laptop);
                laptop.setPrix(new BigDecimal("899.99")); // solde !
                System.out.println("Apres modif: " + laptop);
            }

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    // ex: methode pour supprimer un produit
    private static void supprimerProduit(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            // on supprime le casque audio (id=4)
            Produit casque = em.find(Produit.class, 4L);
            if (casque != null) {
                em.remove(casque);
                System.out.println("\nProduit supprimé: " + casque.getNom());
            }

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }
}
