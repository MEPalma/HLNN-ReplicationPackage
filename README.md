# On-the-Fly Syntax Highlighting Using Neural Networks

[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.6958312.svg)](https://doi.org/10.5281/zenodo.6958312)
[![CC BY 4.0](https://img.shields.io/badge/license-CC%20BY--NC%204.0-lightgrey.svg)](http://creativecommons.org/licenses/by-nc/4.0/)
[![Netlify Status](https://api.netlify.com/api/v1/badges/2a8b2b32-0f90-40d9-a10e-80b246d91714/deploy-status)](https://app.netlify.com/sites/hlnn/deploys)

This repository represents the replication package for the [paper](https://2022.esec-fse.org/details/fse-2022-research-papers/31/On-the-Fly-Syntax-Highlighting-Using-Neural-Networks):

> **On-the-Fly Syntax Highlighting Using Neural Networks**
> 
> With the presence of online collaborative tools for software developers, source code is shared and consulted frequently, from code viewers to merge requests and code snippets. Typically, code highlighting quality in such scenarios is sacrificed in favor of system responsiveness. In these on-the-fly settings, performing a formal grammatical analysis of the source code is expensive and intractable for the many times the input is an invalid derivation of the language. Indeed, current popular highlighters heavily rely on a system of regular expressions, typically far from the specification of the language's lexer. Due to their complexity, regular expressions need to be periodically updated as more feedback is collected from the users and their design unwelcome the detection of more complex language formations. This paper delivers a deep learning-based approach suitable for on-the-fly grammatical code highlighting of correct and incorrect language derivations, such as code viewers and snippets. It focuses on alleviating the burden on the developers, who can reuse the language's parsing strategy to produce the desired highlighting specification. Moreover, this approach is compared to nowadays online syntax highlighting tools and formal methods in terms of accuracy and execution time, across different levels of grammatical coverage, for three mainstream programming languages. The results obtained show how the proposed approach can consistently achieve near-perfect accuracy in its predictions, thereby outperforming regular expression-based strategies.

The paper is published in the proceeding of the *30th ACM Joint European Software Engineering Conference and Symposium on the Foundations of Software Engineering ([ESEC/FSE](https://2022.esec-fse.org))*.

In this replication package, we provide all the data and scripts we used in our study.

## :open_file_folder: Organization

The repository is organized as follows:

* [`src/`](/src) contains the scripts to build and execute the models
* [`docs/`](docs/) contains the source code for the website where we documented our scripts

Additional resources can be found at: https://zenodo.org/record/zenodo.6949491.
In particular:

* The input data that was used for the study
* The obtained detailed results
* The resources to replicate this study by using this repository's code. The input data is formatted to be compatible with the provided code. The content of the file `HLNN-Resources.zip` has to be extracted in the folder `src/main/resources`

## :books: How to cite this dataset

If you would like to cite the dataset, please use the following `BibTeX` snippet:

```bibtex
@article{palma_onthefly_2022,
    author = {Palma, Marco Edoardo and Salza, Pasquale and Gall, Harald C.},
    title = {{On-the-Fly Syntax Highlighting Using Neural Networks}},
    journal = {ACM Joint European Software Engineering Conference and Symposium on the Foundations of Software Engineering (ESEC/FSE)},
    year = {2022},
    doi = {10.1145/3540250.3549109}
}
```

## :balance_scale: License

This replication package is licensed under the terms of the [Creative Commons Attribution-NonCommercial 4.0 International License](http://creativecommons.org/licenses/by-nc/4.0/).
Please see the [LICENSE](LICENSE) file for full details.

## :pray: Credits

* [Marco Edoardo Palma](mailto:marcoepalma@ifi.uzh.ch) - University of Zurich, Switzerland
* [Pasquale Salza](mailto:salza@ifi.uzh.ch) - University of Zurich, Switzerland
* [Harald C. Gall](mailto:gall@ifi.uzh.ch) - University of Zurich, Switzerland
