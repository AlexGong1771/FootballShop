package com.alexapp.footballShop.controllers;

import com.alexapp.footballShop.models.Product;
import com.alexapp.footballShop.models.ProductDto;
import com.alexapp.footballShop.services.ProductsRepo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductsController {

    @Autowired
    private ProductsRepo repo;

    @GetMapping({"/", ""})
    public String showProductList(Model model) {
        List<Product> products = repo.findAll(Sort.by(Sort.Direction.DESC , "id"));
        model.addAttribute("products", products);
        return "products/index";  // This expects src/main/resources/templates/products/index.html
    }

    @GetMapping("/create")
    public String showCreatePage(Model model){
        ProductDto productDto = new ProductDto();
        model.addAttribute("productDto" , productDto);
        return "products/CreateProduct";
    }

    @PostMapping("/create")
    public String createProduct(@Valid @ModelAttribute ProductDto productDto , BindingResult bindingResult){
      if (productDto.getImageFile().isEmpty()){
          bindingResult.addError(new FieldError("productDto" , "imageFile" , "The image file is required"));
      }
      if (bindingResult.hasErrors()){
          return "products/CreateProduct";
      }
        MultipartFile image = productDto.getImageFile();
        Date date = new Date();
        String storage = date.getTime() + "_" + image.getOriginalFilename();
        try {
              String uploadDir = "public/images";
              Path upload = Paths.get(uploadDir);
              if (!Files.exists(upload)){
                  Files.createDirectories(upload);
              }
              try(InputStream inputStream = image.getInputStream()){
                  Files.copy(inputStream , Paths.get(uploadDir + storage),
                  StandardCopyOption.REPLACE_EXISTING);
              }
        }catch (Exception e){
            System.out.println(e.getMessage());
        }

        Product product = new Product();
        product.setName(productDto.getName());
        product.setBrand(productDto.getBrand());
        product.setCategory(productDto.getCategory());
        product.setPrice(productDto.getPrice());
        product.setDescription(productDto.getDescryption());
        product.setCreatedAt(date);
        product.setImageFileName(storage);

        repo.save(product);

        return "redirect:/products";
    }
    @GetMapping("/edit")
    public String showEditPage(Model model , @RequestParam long id){
        try {
            Product product = repo.findById(id).get();
            model.addAttribute("product" , product);

            ProductDto productDto = new ProductDto();
            productDto.setName(product.getName());
            productDto.setBrand(product.getBrand());
            productDto.setCategory(product.getCategory());
            productDto.setPrice(product.getPrice());
            productDto.setDescryption(product.getDescription());

            model.addAttribute("productDto" , productDto);
        }catch (Exception e){
            System.out.println(e.getMessage());
            return "redirect:/products";
        }
        return "products/EditProduct";
    }
    @PostMapping("/edit")
    public String  updateProduct(Model model , @RequestParam long id  , @Valid @ModelAttribute ProductDto productDto ,BindingResult bindingResult){
          try {
              Product product = repo.findById(id).get();
              model.addAttribute("product" , product);

              if (bindingResult.hasErrors()){
                  return  "products/EditProduct";
              }
              if (!productDto.getImageFile().isEmpty()){
                  String uploadDir = "public/images/";
                  Path oldImagePath = Paths.get(uploadDir + product.getImageFileName());

                  try {
                      Files.delete(oldImagePath);
                  }catch (Exception e){
                      System.out.println("Exception: " + e.getMessage());
                  }

                  MultipartFile image = productDto.getImageFile();
                  Date createdAt = new Date();
                  String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();

                  try (InputStream inputStream = image.getInputStream()){
                      Files.copy(inputStream , Paths.get(uploadDir + storageFileName) , StandardCopyOption.REPLACE_EXISTING);

                  }
                  product.setImageFileName(storageFileName);
              }
              product.setName(productDto.getName());
              product.setBrand(productDto.getBrand());
              product.setCategory(productDto.getCategory());
              product.setPrice(productDto.getPrice());
              product.setDescription(productDto.getDescryption());

              repo.save(product);
          }catch (Exception e){
              System.out.println("Exception" +  e.getMessage());
          }

        return "redirect:/products";
    }
    @GetMapping("/delete")
    public String deleteProduct(@RequestParam long id){
        try {
            Product product = repo.findById(id).get();
            Path imagePath = Paths.get("public/images/" + product.getImageFileName());
            try {
                Files.delete(imagePath);

            } catch (Exception e){
                System.out.println("Exception" +  e.getMessage());
            }
            repo.delete(product);

        }catch (Exception e){
            System.out.println("Exception" +  e.getMessage());
        }


        return "redirect:/products";
    }
}
