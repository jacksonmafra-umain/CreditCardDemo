# Credit Card Form Demo

This project is a fully functional and animated credit card form UI built entirely with **Jetpack Compose**, Android's modern UI toolkit. It is a faithful recreation of the popular and beautifully designed Vue.js demo created by **Muhammed Erdem**.

The goal of this project was to replicate the original's fluid animations, real-time updates, and overall user experience using native Android components and the declarative paradigm of Jetpack Compose.

## Inspiration and Credits

This project would not exist without the original and brilliant work of **Muhammed Erdem**. All credit for the UI/UX design, concept, and animation ideas goes to him. This repository is simply an exercise in porting that fantastic experience to modern native Android development.

Please check out his original work:

-   **Original Vue.js GitHub Repository**: [https://github.com/muhammederdem/credit-card-form](https://github.com/muhammederdem/credit-card-form)
-   **Live Codepen Demo**: [https://codepen.io/muhammederdem/pen/bgpPyy](https://codepen.io/muhammederdem/pen/bgpPyy)
-   **Inspiration**: The project was inspired by his [LinkedIn post](https://www.linkedin.com/posts/muhammed-erdem-1b0349123_vue-javascript-codepen-activity-6534333977989324800-z2bA/) showcasing the Vue.js implementation.

## Features

This Android demo replicates all the key features of the original web project:

-   **Interactive Card Preview**: A flippable card that updates in real-time as the user types.
-   **Card Flip Animation**: The card smoothly flips to the back when the CVV field is focused.
-   **Dynamic Card Brand Detection**: The card logo (Visa, Mastercard, Amex) changes automatically based on the card number.
-   **Input Formatting & Masking**: The card number and expiry date are automatically formatted with spaces and slashes for better readability using a `VisualTransformation`.
-   **"Rolling" Number Animation**: Replicates the original `slide-fade-up` effect for numbers using `AnimatedContent` in Compose, giving the digits a smooth "rolling" feel as they appear.
-   **Network Image Loading**: Uses [Coil](https://coil-kt.github.io/coil/) to dynamically load card backgrounds, logos, and the chip image from the original project's GitHub repository, just like the web version.
