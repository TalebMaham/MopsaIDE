# MopsaIDE :

<h3 style="color:red">Resumé : </h3>
<p>
Le projet que nous avons réalisé dans le cadre du module PSTL de M1 STL à Sorbonne Université consiste à implémenter un outil d'analyse statique pour les programmes écrits en langages C et Python. Cet outil, appelé Mopsa, permet de détecter les erreurs potentielles dans le code avant l'exécution, ce qui peut contribuer à améliorer la qualité du logiciel. Cependant, jusqu'à présent, Mopsa ne pouvait être exécuté que dans un terminal, ce qui limitait son utilisation pratique.

Pour résoudre ce problème, notre projet consiste à développer un code, appelé MopsaIDE, qui utilise la bibliothèque MagpieBridge pour connecter Mopsa à tout environnement de développement intégré (IDE) via le protocole Language Server Protocol (LSP). MagpieBridge est une bibliothèque écrite en Java qui agit comme un adaptateur entre Mopsa et l'IDE, ce qui permet à l'utilisateur de voir les résultats de l'analyse dans l'éditeur de code de l'IDE plutôt que dans le terminal.

Notre projet a pour but de rendre l'utilisation de Mopsa plus pratique et plus efficace pour les développeurs, en leur permettant d'utiliser leur IDE préféré pour écrire du code tout en ayant accès aux résultats de l'analyse en temps réel. Dans ce rapport, nous allons détailler notre processus de développement, les problèmes que nous avons rencontrés et les solutions que nous avons trouvées, ainsi que les résultats obtenus.
</p>

<p>
Notre travail a commencé par l'examen de la documentation de MagpieBridge pour comprendre comment utiliser cette bibliothèque dans notre projet. Cependant, nous avons rapidement réalisé que l'utilisation de MagpieBridge nécessite l'utilisation d'un gestionnaire de projet appelé Maven, avec lequel nous n'étions pas familiers. Nous avons donc passé du temps à apprendre comment utiliser Maven et à comprendre le fichier pom.xml pour ajouter toutes les dépendances nécessaires.

Ensuite, nous avons cherché à installer un exemple de projet utilisant MagpieBridge pour comprendre comment notre propre logiciel serait installé une fois terminé et pour clarifier ce qui est attendu de notre part. Nous avons commencé par SootIDE, mais avons rencontré des difficultés lors de son installation, ce qui rend l'installation toujours en cours. En parallèle, nous avons également tenté d'installer InferIDE avec l'analyseur Infer produit par Facebook, mais l'installation n'était pas parfaite car les résultats d'analyse ont été affichés dans un fichier .log plutôt que dans l'IDE. Nous travaillons actuellement pour résoudre ce problème.

Finalement, nous avons décidé de nous inspirer de ce que les autres développeurs ont fait avec Infer pour faire la même chose avec Mopsa. Comme Infer, Mopsa est écrit en OCaml, ce qui nous a permis de comprendre comment les développeurs ont utilisé Infer pour établir une connexion entre l'analyseur et l'IDE.
</p>


<p>
Mopsa peut fournir des résultats d'analyse dans différents formats. Nous avons choisi le format JSON pour faciliter le traitement ultérieur des résultats d'analyse. Cependant, nous avons rencontré des difficultés pour obtenir directement les résultats d'analyse au format JSON. Pour contourner ce problème, nous avons décidé de stocker les résultats d'analyse dans un fichier "analyse.json" pour ensuite les traiter. Nous avons ainsi pu obtenir des objets JSON pour les résultats d'analyse. Cependant, la structure d'analyse de Mopsa est différente de celle d'Infer. Nous travaillons actuellement sur l'adaptation des données d'objet JSON obtenues à partir de Mopsa afin qu'elles puissent être utilisées avec notre projet.
</p>

