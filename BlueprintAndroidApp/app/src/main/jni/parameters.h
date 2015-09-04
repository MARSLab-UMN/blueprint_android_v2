#ifndef _PARAMETERS_H_
#define _PARAMETERS_H_

//#include "configuration-file.h"
#include <string>

class Parameters {
public:
    Parameters() {
    }

    static Parameters *instance() {
        static Parameters instance;
        return &instance;
    }

    std::string dataset_path;
    int num_of_imgs;
    int img_subsampling;
    int start_of_rotation;
    int start_of_stairs;


};
//
//inline void LoadParameters(std::string file_path, Parameters *parameters) {
//    ConfigurationFile *configuration_file = new ConfigurationFile(file_path);
//
//    parameters->dataset_path =
//        configuration_file->read < std::string >("dataset_path");
//    parameters->num_of_imgs =
//        configuration_file->read < int >("num_of_imgs");
//    parameters->img_subsampling =
//        configuration_file->read < int >("img_subsampling");
//    parameters->start_of_rotation =
//            configuration_file->read < int >("start_of_rotation");
//    parameters->start_of_stairs =
//            configuration_file->read < int >("start_of_stairs");
//
//    delete configuration_file;
//}


#endif
