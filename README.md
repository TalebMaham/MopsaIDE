# MopsaIDE :

<h3 style="color:red">Introduction</h3>
<p>
Le projet que nous avons réalisé dans le cadre du module PSTL de M1 STL à Sorbonne Université consiste à implémenter un outil d'analyse statique pour les programmes écrits en langages C et Python. Cet outil, appelé Mopsa, permet de détecter les erreurs potentielles dans le code avant l'exécution, ce qui peut contribuer à améliorer la qualité du logiciel. Cependant, jusqu'à présent, Mopsa ne pouvait être exécuté que dans un terminal, ce qui limitait son utilisation pratique.

Pour résoudre ce problème, notre projet consiste à développer un code, appelé MopsaIDE, qui utilise la bibliothèque MagpieBridge pour connecter Mopsa à tout environnement de développement intégré (IDE) via le protocole Language Server Protocol (LSP). MagpieBridge est une bibliothèque écrite en Java qui agit comme un adaptateur entre Mopsa et l'IDE, ce qui permet à l'utilisateur de voir les résultats de l'analyse dans l'éditeur de code de l'IDE plutôt que dans le terminal.

Notre projet a pour but de rendre l'utilisation de Mopsa plus pratique et plus efficace pour les développeurs, en leur permettant d'utiliser leur IDE préféré pour écrire du code tout en ayant accès aux résultats de l'analyse en temps réel. Dans ce rapport, nous allons détailler notre processus de développement, les problèmes que nous avons rencontrés et les solutions que nous avons trouvées, ainsi que les résultats obtenus.
</p>


