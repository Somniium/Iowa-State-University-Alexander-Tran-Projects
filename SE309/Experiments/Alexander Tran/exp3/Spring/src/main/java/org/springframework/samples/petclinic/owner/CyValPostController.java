/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 * @Modified By Tanmay Ghosh
 * @Modified By Vivek Bengre
 * @Modified By Alexander Tran
 */
@RestController
public class CyValPostController {

    @Autowired
    private CyValPostRepository postRepo;

    // CREATE (POST)
    @PostMapping("/posts/new")
    public String createPost(@RequestBody CyValPost post) {
        postRepo.save(post);
        return "New CyVal post saved: " + post.getTitle();
    }

     // function just to create dummy data
    @GetMapping("/post/create")
    public String createDummyData() {
        postRepo.save(new CyValPost(
                "Favorite albums this week?",
                "Drop recs and explain why you like them.",
                "MUSIC",
                "alextran",
                "PUBLIC"
        ));

        postRepo.save(new CyValPost(
                "Paper discussion: distributed systems",
                "What concept stood out to you?",
                "ACADEMIC_PAPER",
                "isuStudent42",
                "PUBLIC"
        ));
        return "Dummy CyVal posts created";
    }

    // READ ALL (GET)
    @GetMapping("/posts")
    public List<CyValPost> getAllPosts() {
        return postRepo.findAll();
    }

    // READ ONE (GET)
    @GetMapping("/posts/{id}")
    public Optional<CyValPost> getPostById(@PathVariable int id) {
        return postRepo.findById(id);
    }

}
